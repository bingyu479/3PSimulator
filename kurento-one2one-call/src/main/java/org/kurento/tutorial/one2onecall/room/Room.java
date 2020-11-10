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
	private AlexaUserSession patient;
	private boolean providerJoined;
	private boolean alexaJoined;

	private final String roomName;

	public Room(String name, CallMediaPipeline callMediaPipeline) {
		this.roomName = name;
		this.alexaJoined = false;
		this.providerJoined = false;
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
		this.providerJoined = true;
		log.info("Provider {} has joined", userName);
	}

	public void joinAsAlexa(String userName, AlexaUserSession userSession) {
		this.patient = userSession;
		this.alexaJoined = true;
		log.info("Alexa user {} has joined", userName);
	}

	public boolean hasAlexaJoined() {
		return this.alexaJoined;
	}

	public boolean hasProviderJoined() {
		return this.providerJoined;
	}

	public AlexaUserSession getPatient() {
		return patient;
	}

	public WebUserSession getProvider() {
		return provider;
	}

	@Override
	public void close() {

		provider = null;
		patient = null;

		log.info("Room {} closed", this.roomName);
	}
}
