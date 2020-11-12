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

package org.kurento.tutorial.one2onecall.users;

import java.util.concurrent.ConcurrentHashMap;

import org.kurento.tutorial.one2onecall.users.AlexaUserSession;
import org.kurento.tutorial.one2onecall.users.UserSession;
import org.kurento.tutorial.one2onecall.users.WebUserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

/**
 * Map of users registered in the system. This class has a concurrent hash map to store users, using
 * its name as key in the map.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class UserRegistry {

	private static final Logger log = LoggerFactory.getLogger(UserRegistry.class);

	private ConcurrentHashMap<String, UserSession> usersByName = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, WebSocketSession> wsSessionByName = new ConcurrentHashMap<>();

	public void registerAlexaUser(AlexaUserSession user) {
		usersByName.put(user.getName(), user);
	}

	public void registerWebUser(WebUserSession user) {
		usersByName.put(user.getName(), user);
		usersBySessionId.put(user.getSession().getId(), user);
		wsSessionByName.put(user.getName(), user.getSession());
		log.info("User {} in UserRegistry", user.getName());
	}

	public UserSession getBySession(WebSocketSession session) {
		return usersBySessionId.get(session.getId());
	}

	public WebSocketSession getWSSessionByName(String name) {
		return wsSessionByName.get(name);
	}

	public boolean exists(String name) {
		return usersByName.keySet().contains(name);
	}

	public UserSession removeBySession(WebSocketSession session) {
		final UserSession user = getBySession(session);
		if (user != null) {
			usersByName.remove(user.getName());
			usersBySessionId.remove(session.getId());
		}
		return user;
	}

}
