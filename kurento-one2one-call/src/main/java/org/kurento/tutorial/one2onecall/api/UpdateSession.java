package org.kurento.tutorial.one2onecall.api;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.tutorial.one2onecall.CallMediaPipeline;
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
public class UpdateSession {

    private static final Logger log = LoggerFactory.getLogger(UpdateSession.class);

    @Autowired
    private UserRegistry registry;

    @Autowired
    private RoomManager roomManager;


    @PostMapping(value = "/alexa/telehealth/session/update",
        consumes= "application/json", produces = "application/json")
    public TelehealthSessionResponse updateSessionWithOffer(@RequestBody TelehealthSessionRequest updateSession) {
        log.info("Alexa user {} is updating a session {} with an SDP offer",
            updateSession.getUserName(), updateSession.getSessionId());

        // Find the room
        Room room = roomManager.getRoomOrThrow(updateSession.getSessionId());
        CallMediaPipeline callMediaPipeline = room.getCallMediaPipeline();
        callMediaPipeline.updateAlexaWebRtcEp();

        // Find alexa user session
        AlexaUserSession alexa = room.getAlexa();
        alexa.setSdpOffer(updateSession.getSdpOffer());
        alexa.setWebRtcEndpoint(callMediaPipeline.getAlexaWebRtcEp());

        // Generate SDP answer for the updated offer
        log.info("Generating updated SDP answer for Alexa");
        String alexaSdpAnswer = callMediaPipeline.getAlexaWebRtcEp().processOffer(updateSession.getSdpOffer());
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


        // Trigger a re-negotiate process on provider side
        room.getProvider().setWebRtcEndpoint(callMediaPipeline.getProviderWebRtcEp());
        callMediaPipeline.getProviderWebRtcEp().addIceCandidateFoundListener(
            new EventListener<IceCandidateFoundEvent>() {
                @Override
                public void onEvent(IceCandidateFoundEvent event) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", "iceCandidate");
                    response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));

                    try {
                        // Send KMS's ice candidate to local peer
                        room.getProvider().sendMessage(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        String providerSdpOffer = callMediaPipeline.getProviderWebRtcEp().generateOffer();
        log.info("Generating a new offer to provider. {}", providerSdpOffer);
        JsonObject updatedSdpOffer = new JsonObject();
        updatedSdpOffer.addProperty("id", "updatedSdpOffer");
        updatedSdpOffer.addProperty("sdpOffer", providerSdpOffer);

        try {
            room.getProvider().sendMessage(updatedSdpOffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        callMediaPipeline.getProviderWebRtcEp().gatherCandidates();

		alexa.answerGeneratedForSession(alexaSdpAnswer, registry);
        return new TelehealthSessionResponse(updateSession.getUserName(), updateSession.getSessionId(), alexaSdpAnswer);
    }
}
