package org.kurento.tutorial.one2onecall.room;

import java.io.Closeable;
import org.kurento.tutorial.one2onecall.CallMediaPipeline;
import org.kurento.tutorial.one2onecall.users.AlexaUserSession;
import org.kurento.tutorial.one2onecall.users.WebUserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Room implements Closeable {

	private final Logger log = LoggerFactory.getLogger(Room.class);

	private CallMediaPipeline callMediaPipeline;
	private WebUserSession provider;
	private AlexaUserSession alexa;

	private final String roomName;

	public Room(String name, CallMediaPipeline callMediaPipeline) {
		this.roomName = name;
		this.callMediaPipeline = callMediaPipeline;
	}

	public String getRoomName() {
		return roomName;
	}

	public CallMediaPipeline getCallMediaPipeline() {
		return callMediaPipeline;
	}

	public void joinAsProvider(String userName, WebUserSession userSession) {
		this.provider = userSession;
		log.info("Provider {} has joined", userName);
	}

	public void joinAsAlexa(String userName, AlexaUserSession userSession) {
		this.alexa = userSession;
		log.info("Alexa user {} has joined", userName);
	}

	public AlexaUserSession getAlexa() {
		return alexa;
	}

	public WebUserSession getProvider() {
		return provider;
	}

	@Override
	public void close() {

		provider = null;
		alexa = null;
		callMediaPipeline = null;

		log.info("Room {} closed", this.roomName);
	}
}
