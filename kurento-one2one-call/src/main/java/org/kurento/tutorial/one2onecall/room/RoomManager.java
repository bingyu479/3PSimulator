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

package org.kurento.tutorial.one2onecall.room;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.kurento.client.KurentoClient;
import org.kurento.tutorial.one2onecall.CallMediaPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Ivan Gracia (izanmail@gmail.com)
 * @since 4.3.1
 */
public class RoomManager {

	private final Logger log = LoggerFactory.getLogger(RoomManager.class);

	@Autowired
	private KurentoClient kurento;

	private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

	/**
	 * Looks for a room in the active room list.
	 *
	 * @param roomName the name of the room
	 * @return the room if it was already created, or a new one if it is the first time this room is
	 * accessed
	 */
	public Room getRoomOrCreate(String roomName) {
		log.info("Searching for room {}", roomName);
		Room room = rooms.get(roomName);

		if (room == null) {
			log.info("Room {} not exist. Will create now!", roomName);
			room = new Room(roomName, new CallMediaPipeline(kurento));
			rooms.put(roomName, room);
		}
		log.info("Room {} found!", roomName);
		return room;
	}

	public Room getRoomOrThrow(String roomName) {
        log.info("Searching for room {}", roomName);
        Room room = rooms.get(roomName);

        if (room == null) {
            throw new IllegalStateException(String.format("The session %s doesn't exist anymore.", roomName));
        }
        log.info("Room {} found!", roomName);
        return room;
    }

	/**
	 * Removes a room from the list of available rooms.
	 *
	 * @param room the room to be removed
	 */
	public void removeRoom(Room room) {
		room.close();
		this.rooms.remove(room.getRoomName());
		log.info("Room {} removed and closed", room.getRoomName());
	}

}
