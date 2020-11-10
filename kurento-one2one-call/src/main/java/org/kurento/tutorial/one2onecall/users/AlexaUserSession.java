package org.kurento.tutorial.one2onecall.users;

import com.google.gson.JsonObject;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class AlexaUserSession extends UserSession {

	private static final Logger log = LoggerFactory.getLogger(AlexaUserSession.class);


	public AlexaUserSession(String name, String sdpOffer) {
		super(name);
		this.sdpOffer = sdpOffer;
	}

	public void answerGeneratedForSession(AlexaUserSession alexa, String sdpAnswer,
		UserRegistry registry) {
		log.info("Returning sdp answer {}", sdpAnswer);

		JsonObject startCommunication = new JsonObject();
		startCommunication.addProperty("id", "startCommunication");
		startCommunication.addProperty("sdpAnswer", sdpAnswer);

		sendMessage(alexa, startCommunication, registry);
	}

	public void sendMessage(AlexaUserSession alexa, JsonObject startCommunication,
		UserRegistry registry) {
		WebSocketSession alexaWSSession = registry.getWSSessionByName(alexa.getName());

		try {
			synchronized (alexaWSSession) {
				alexaWSSession.sendMessage(new TextMessage(startCommunication.toString()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
