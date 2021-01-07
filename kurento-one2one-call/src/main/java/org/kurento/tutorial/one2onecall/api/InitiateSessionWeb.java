package org.kurento.tutorial.one2onecall.api;

import java.util.concurrent.CountDownLatch;
import org.kurento.tutorial.one2onecall.CallMediaPipeline;
import org.kurento.tutorial.one2onecall.dynamodb.dao.MeetingRoomDAO;
import org.kurento.tutorial.one2onecall.models.TelehealthSessionRequest;
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
public class InitiateSessionWeb {

    @Autowired
    private UserRegistry registry;

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private MeetingRoomDAO meetingRoomDAO;

    private static final Logger log = LoggerFactory.getLogger(InitiateSession.class);

    @PostMapping(value = "/alexa/telehealth/webSession/initiate",
        consumes= "application/json", produces = "application/json")
    public TelehealthSessionResponse initiateWebSessionWithOffer(
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
        Room room = roomManager.getRoomOrCreate(initiateSession.getSessionId());
        room.joinAsAlexa(initiateSession.getUserName(), alexaUserSession);

        // start the call

        AlexaUserSession alexa = room.getAlexa();
        CallMediaPipeline callMediaPipeline = room.getCallMediaPipeline();

        // Generate SDP answer for callee (Alexa) and return
        alexa.setWebRtcEndpoint(callMediaPipeline.getAlexaWebRtcEp());

        log.info("Alexa SDP: {} ", alexa.getSdpOffer());
        log.info("Generating SDP answer for Alexa");
        String alexaSdpAnswer = callMediaPipeline.getAlexaWebRtcEp().processOffer(initiateSession.getSdpOffer());
        final CountDownLatch latch = new CountDownLatch(1);
        callMediaPipeline.getAlexaWebRtcEp().addOnIceGatheringDoneListener(event -> {
            latch.countDown();
        });

        callMediaPipeline.getAlexaWebRtcEp().gatherCandidates();
        try {
            latch.await();
        } catch (InterruptedException e) {
            // Should not reach here
        }

        alexaSdpAnswer = callMediaPipeline.getAlexaWebRtcEp().getLocalSessionDescriptor();
		alexa.answerGeneratedForSession(alexaSdpAnswer, registry);
        return new TelehealthSessionResponse(initiateSession.getUserName(), initiateSession.getSessionId(), alexaSdpAnswer);
    }

}
