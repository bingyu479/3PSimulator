/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.kurento.tutorial.one2onecall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.KurentoClient;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.tutorial.one2onecall.room.Room;
import org.kurento.tutorial.one2onecall.room.RoomManager;
import org.kurento.tutorial.one2onecall.users.AlexaUserSession;
import org.kurento.tutorial.one2onecall.users.UserRegistry;
import org.kurento.tutorial.one2onecall.users.UserSession;
import org.kurento.tutorial.one2onecall.users.WebUserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Protocol handler for 1 to 1 video call communication.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class CallHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(CallHandler.class);
	private static final Gson gson = new GsonBuilder().create();

	@Autowired
	private KurentoClient kurento;

	@Autowired
	private UserRegistry registry;

	@Autowired
	private RoomManager roomManager;

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
		UserSession user = registry.getBySession(session);

		if (user != null) {
			log.debug("Incoming message from user '{}': {}", user.getName(), jsonMessage);
		} else {
			log.debug("Incoming message from new user: {}", jsonMessage);
		}

		switch (jsonMessage.get("id").getAsString()) {
			case "register":
				try {
					registerWebUser(session, jsonMessage);
				} catch (Throwable t) {
					handleErrorResponse(t, session, "registerResponse");
				}
				break;
			case "providerJoinSession":
				joinRoom(user, jsonMessage);
				providerJoinRoom(jsonMessage.getAsJsonPrimitive("room").getAsString());
				break;
			case "onIceCandidate":
				// Received local candidate
				JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
				if (user != null) {
					IceCandidate iceCandidate = new IceCandidate(candidate.get("candidate").getAsString(), candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
					user.addCandidate(iceCandidate);
				}
				break;
			case "stop":
				stop(session);
				break;
			default:
				break;
		}
	}

	// If doctor join later
	private void providerJoinRoom(String roomName){
		Room room = roomManager.getRoom(roomName);

		WebUserSession provider = room.getProvider();

		// Start the session
		CallMediaPipeline callMediaPipeline = room.getCallMediaPipeline();
		provider.setWebRtcEndpoint(callMediaPipeline.getProviderWebRtcEp());

		provider.getWebRtcEndpoint().setStunServerAddress("3.237.192.82");
		provider.getWebRtcEndpoint().setStunServerPort(4172);
		provider.getWebRtcEndpoint().setTurnUrl("1605127832:tk9fafcdbf-404c-4bda-96d2-402dd262fc0a-us-east-1_1605122432579_0:qYmEjPp03svJDe66GWG4v2q2fGk=@3.81.228.72:443?transport=tcp");

		provider.getWebRtcEndpoint().addIceCandidateFoundListener(
			new EventListener<IceCandidateFoundEvent>() {
				@Override
				public void onEvent(IceCandidateFoundEvent event) {
					JsonObject response = new JsonObject();
					response.addProperty("id", "iceCandidate");
					response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));

					try {
						// Send KMS's ice candidate to local peer
						provider.sendMessage(response);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

		// Generate SDP answer for caller(Provider)
		log.info("Provider SDP: {} ", provider.getSdpOffer());
		String callerSdpAnswer = provider.getWebRtcEndpoint().processOffer(provider.getSdpOffer());
		JsonObject startCommunication = new JsonObject();
		startCommunication.addProperty("id", "startCommunication");
		startCommunication.addProperty("sdpAnswer", callerSdpAnswer);

		try {
			provider.sendMessage(startCommunication);
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("SDP answer is generated for provider");
		provider.getWebRtcEndpoint().gatherCandidates();
	}

	private void handleErrorResponse(Throwable throwable, WebSocketSession session,
		String responseId)
		throws IOException {
		log.error(throwable.getMessage(), throwable);
		JsonObject response = new JsonObject();
		response.addProperty("id", responseId);
		response.addProperty("response", "rejected");
		response.addProperty("message", throwable.getMessage());
		session.sendMessage(new TextMessage(response.toString()));
	}

	private void registerWebUser(WebSocketSession session, JsonObject jsonMessage)
		throws IOException {
		String name = jsonMessage.getAsJsonPrimitive("name").getAsString();

		WebUserSession caller = new WebUserSession(session, name, null);
		String responseMsg = "accepted";
		if (name.isEmpty()) {
			responseMsg = "rejected: empty user name";
		} else if (registry.exists(name)) {
			responseMsg = "rejected: user '" + name + "' already registered";
		} else {
			registry.registerWebUser(caller);
		}

		log.info("Web user {} has been registered successfully", name);
		JsonObject response = new JsonObject();
		response.addProperty("id", "registerResponse");
		response.addProperty("response", responseMsg);
		caller.sendMessage(response);
	}

	private WebUserSession joinRoom(UserSession session, JsonObject jsonMessage) {
		String providerName = jsonMessage.getAsJsonPrimitive("provider").getAsString();
		String roomName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();

		Room room = roomManager.getRoom(roomName);
		WebUserSession webUserSession = (WebUserSession) session;
		webUserSession.setSdpOffer(sdpOffer);
		room.joinAsProvider(providerName, webUserSession);
		webUserSession.setRoomName(roomName);
		return webUserSession;
	}

	public void stop(WebSocketSession session) throws IOException {
		// Both users can stop the communication. A 'stopCommunication'
		// message will be sent to the other peer.
		UserSession stopperUser = registry.getBySession(session);

		Room room = roomManager.getRoom(stopperUser.getRoomName());
		if (stopperUser instanceof WebUserSession) {
			// if stopperUser is a provider, stoppedUser should be Alexa
			AlexaUserSession stoppedUser = room.getAlexa();
			stoppedUser.leaveRoom(registry);
		} else {
			WebUserSession stoppedUser = room.getProvider();
			stoppedUser.leaveRoom();
		}
		room.getCallMediaPipeline().release();
		roomManager.removeRoom(room);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
		throws Exception {
		registry.removeBySession(session);
	}

}
