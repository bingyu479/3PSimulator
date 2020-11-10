package org.kurento.tutorial.one2onecall.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.KurentoClient;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.tutorial.one2onecall.CallMediaPipeline;
import org.kurento.tutorial.one2onecall.data.InitiateSessionRequest;
import org.kurento.tutorial.one2onecall.room.Room;
import org.kurento.tutorial.one2onecall.room.RoomManager;
import org.kurento.tutorial.one2onecall.users.AlexaUserSession;
import org.kurento.tutorial.one2onecall.users.UserRegistry;
import org.kurento.tutorial.one2onecall.users.WebUserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InitiateSession {

	@Autowired
	private KurentoClient kurento;

	@Autowired
	private UserRegistry registry;

	@Autowired
	private RoomManager roomManager;

	private static final Logger log = LoggerFactory.getLogger(InitiateSession.class);
	private static final Gson gson = new GsonBuilder().create();

	@PostMapping(value = "/alexa/telehealth/session/initiate", consumes= "application/json")
	public void initiateSessionWithOffer(
		@RequestBody InitiateSessionRequest initiateSession) {
		log.info("Alexa user {} started a session {} with an SDP offer",
			initiateSession.getUserName(), initiateSession.getRoomName());

		// Return DeferredResponse

		// Register Alexa user
		AlexaUserSession alexaUserSession = new AlexaUserSession(initiateSession.getUserName(),
			initiateSession.getSdpOffer());
		log.info("alexa user session is {}" + alexaUserSession.toString());
		registry.registerAlexaUser(alexaUserSession);
		log.info("Alexa user {} has been registered successfully", initiateSession.getUserName());

		// Join room
		Room room = roomManager.getRoom(initiateSession.getRoomName());
		room.joinAsAlexa(initiateSession.getUserName(), alexaUserSession);

		// start the call

		if (room.hasProviderJoined()) {
			WebUserSession provider = room.getProvider();
			AlexaUserSession alexa = room.getPatient();
			// For caller (Alexa)
			CallMediaPipeline callMediaPipeline = new CallMediaPipeline(kurento);
			alexa.setWebRtcEndpoint(callMediaPipeline.getProviderWebRtcEp());
			// Generate SDP answer for caller (Alexa) and return
			log.info("Generating SDP answer for Alexa");
			log.info("Alexa SDP: {} ", alexa.getSdpOffer());
			String callerSdpAnswer = callMediaPipeline
				.generateSdpAnswerForCaller(alexa.getSdpOffer());
			alexa.answerGeneratedForSession(alexa, callerSdpAnswer, registry);

			// For callee (provider)
			provider.setWebRtcEndpoint(callMediaPipeline.getAlexaWebRtcEp());
			callMediaPipeline.getAlexaWebRtcEp().addIceCandidateFoundListener(
				new EventListener<IceCandidateFoundEvent>() {
					@Override
					public void onEvent(IceCandidateFoundEvent event) {
						JsonObject response = new JsonObject();
						response.addProperty("id", "iceCandidate");
						response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));

						try {
							provider.sendMessage(response);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});

			//user:password@ipaddress:port?transport=[udp|tcp|tls]
			callMediaPipeline.getAlexaWebRtcEp().setTurnUrl("1604977009:tkea382e6a-7356-4c62-993f-18a8138196d4-us-east-1_1604971609396_0:Io2XMhUf0dfO1UlLm7YnDqSode0=@54.86.214.95:443?transport=tcp");
			callMediaPipeline.getAlexaWebRtcEp().setStunServerAddress("52.4.4.85");
			callMediaPipeline.getAlexaWebRtcEp().setStunServerPort(4172);
			// Generate SDP answer for callee (Alexa)
			log.info("Provider SDP: {} ", provider.getSdpOffer());
			String calleeSdpAnswer = callMediaPipeline
				.generateSdpAnswerForCallee(provider.getSdpOffer());
			JsonObject startCommunication = new JsonObject();
			startCommunication.addProperty("id", "startCommunication");
			startCommunication.addProperty("sdpAnswer", calleeSdpAnswer);
			callMediaPipeline.getProviderWebRtcEp().gatherCandidates();
			try {
				provider.sendMessage(startCommunication);
			} catch (IOException e) {
				e.printStackTrace();
			}
			log.info("SDP answer is generated for Provider");

		}
	}
}
