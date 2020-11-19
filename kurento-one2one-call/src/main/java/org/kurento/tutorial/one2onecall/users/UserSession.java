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

import java.util.ArrayList;
import java.util.List;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;

/**
 * User session.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class UserSession {

	protected final String name;

	protected String sdpOffer;
	protected String roomName;
	protected WebRtcEndpoint webRtcEndpoint;
	protected final List<IceCandidate> candidateList = new ArrayList<IceCandidate>();

	public UserSession(String name, String roomName) {
		this.name = name;
		this.roomName = roomName;
	}

	public String getName() {
		return name;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getSdpOffer() {
		return sdpOffer;
	}

	public void setSdpOffer(String sdpOffer) {
		this.sdpOffer = sdpOffer;
	}

	public WebRtcEndpoint getWebRtcEndpoint() {
		return webRtcEndpoint;
	}

	public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
		this.webRtcEndpoint = webRtcEndpoint;

		for (IceCandidate e : candidateList) {
			this.webRtcEndpoint.addIceCandidate(e);
		}
	}

	public void addCandidate(IceCandidate candidate) {
		if (this.webRtcEndpoint != null) {
			this.webRtcEndpoint.addIceCandidate(candidate);
		} else {
			candidateList.add(candidate);
		}
	}
}
