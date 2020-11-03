package org.kurento.tutorial.groupcall.api;

import com.google.gson.JsonObject;
import java.io.IOException;
import org.kurento.tutorial.groupcall.CallHandler;
import org.kurento.tutorial.groupcall.Room;
import org.kurento.tutorial.groupcall.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;

@RestController
public class InitiateSession {

    private static final Logger log = LoggerFactory.getLogger(CallHandler.class);

    @PostMapping("/alexa/telehealth/session/initiate")
    public void initiateSessionWithOffer(@RequestBody String sdpOffer) {
        log.info("Got initiateSessionWithOffer, initiating a new session with SDP offer: {}", sdpOffer);

    }

//    private void joinRoom(JsonObject params, WebSocketSession session) throws IOException {
//        final String roomName = params.get("room").getAsString();
//        final String name = params.get("name").getAsString();
//        log.info("PARTICIPANT {}: trying to join room {}", name, roomName);
//
//        // Get or create new room
//        Room room = roomManager.getRoom(roomName);
//        final UserSession user = room.join(name, session);
//        registry.register(user);
//    }

}
