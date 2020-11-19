package org.kurento.tutorial.one2onecall.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.tutorial.one2onecall.CallMediaPipeline;
import org.kurento.tutorial.one2onecall.dynamodb.dao.MeetingRoomDAO;
import org.kurento.tutorial.one2onecall.models.TelehealthSessionRequest;
import org.kurento.tutorial.one2onecall.models.TelehealthSessionRequest.IceServer;
import org.kurento.tutorial.one2onecall.models.TelehealthSessionResponse;
import org.kurento.tutorial.one2onecall.room.Room;
import org.kurento.tutorial.one2onecall.room.RoomManager;
import org.kurento.tutorial.one2onecall.users.AlexaUserSession;
import org.kurento.tutorial.one2onecall.users.UserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InitiateSession {

	@Autowired
	private UserRegistry registry;

	@Autowired
	private RoomManager roomManager;

	@Autowired
	private MeetingRoomDAO meetingRoomDAO;

	private static final Logger log = LoggerFactory.getLogger(InitiateSession.class);

	@PostMapping(value = "/alexa/telehealth/session/initiate",
		consumes= "application/json", produces = "application/json")
	public TelehealthSessionResponse initiateSessionWithOffer(
		@RequestBody TelehealthSessionRequest initiateSession) {
		log.info("Alexa user {} started a session {} with an SDP offer",
			initiateSession.getUserName(), initiateSession.getSessionId());

		// Check appointment schedule

		// Write into TelehealthSessionTable

		// Write into MeetingRoom table and get room name
//		meetingRoomDAO.addPatientToMeetingRoom(initiateSession.getUserName(), initiateSession.getSessionId());

		// Register Alexa user
		AlexaUserSession alexaUserSession = new AlexaUserSession(initiateSession.getUserName(), initiateSession.getSessionId(),
			initiateSession.getSdpOffer());
		log.info("alexa user room is {}", alexaUserSession.getRoomName());
		registry.registerAlexaUser(alexaUserSession);
		log.info("Alexa user {} has been registered successfully", initiateSession.getUserName());

		// Join room
		Room room = roomManager.getRoom(initiateSession.getSessionId());
		room.joinAsAlexa(initiateSession.getUserName(), alexaUserSession);

		// start the call

		AlexaUserSession alexa = room.getAlexa();
		CallMediaPipeline callMediaPipeline = room.getCallMediaPipeline();
		if (initiateSession.getIceServers() != null) {
			setIceServers(initiateSession.getIceServers(), callMediaPipeline.getProviderWebRtcEp());
		}

		// Generate SDP answer for callee (Alexa) and return
		alexa.setWebRtcEndpoint(callMediaPipeline.getAlexaWebRtcEp());

		log.info("Alexa SDP: {} ", alexa.getSdpOffer());
		log.info("Generating SDP answer for Alexa");
		String alexaSdpAnswer = alexa.getWebRtcEndpoint().processOffer(initiateSession.getSdpOffer());
		final CountDownLatch latch = new CountDownLatch(1);
		alexa.getWebRtcEndpoint().addOnIceGatheringDoneListener(event -> {
			latch.countDown();
		});

		alexa.getWebRtcEndpoint().gatherCandidates();
		try {
			latch.await();
		} catch (InterruptedException e) {
			// Should not reach here
		}

		alexaSdpAnswer = alexa.getWebRtcEndpoint().getLocalSessionDescriptor();
		alexa.answerGeneratedForSession(alexaSdpAnswer, registry);
		return new TelehealthSessionResponse(initiateSession.getUserName(), initiateSession.getSessionId(), alexaSdpAnswer);
	}

	private void setIceServers(IceServer[] iceServers, WebRtcEndpoint providerWebRtcEp) {
		try {
			for (IceServer iceServer : iceServers) {
				if (iceServer.getUrl().startsWith("stun")) {
					//url: stun:mrs-2a687c05-1.p.us-east-1.cmds-tachyon.com:4172
					String stunUrl = iceServer.getUrl();
					String[] stun = stunUrl.split(":");
					InetAddress inetAddress = InetAddress.getByName(stun[1]);
					providerWebRtcEp.setStunServerAddress(inetAddress.getHostAddress());
					log.info("Stun: {} {}", inetAddress.getHostAddress(), stun[2]);
					providerWebRtcEp.setStunServerPort(Integer.parseInt(stun[2]));
				} else {
					// url: turns:mrs-2a687c05-1.p.us-east-1.cmds-tachyon.com:443?transport=tcp
					// username: 1605127832:tk9fafcdbf-404c-4bda-96d2-402dd262fc0a-us-east-1_1605122432579_0
					// credential: qYmEjPp03svJDe66GWG4v2q2fGk=
					String[] turn = iceServer.getUrl().split(":");
					InetAddress inetAddress = InetAddress.getByName(turn[1]);
					String newTurnUrl =
						iceServer.getUsername() + ":" + iceServer.getCredential() + "@"
							+ inetAddress.getHostAddress() + ":" + turn[2];
					log.info("Turn url: {}", newTurnUrl);
					providerWebRtcEp.setTurnUrl(newTurnUrl);
				}
			}
		} catch (UnknownHostException e) {
			log.error("UnknownHostException", e);
		}
	}
}
