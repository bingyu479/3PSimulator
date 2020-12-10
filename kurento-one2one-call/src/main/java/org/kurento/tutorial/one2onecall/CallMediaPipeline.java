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

package org.kurento.tutorial.one2onecall;

import org.kurento.client.GStreamerFilter;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

/**
 * Media Pipeline (WebRTC endpoints, i.e. Kurento Media Elements) and connections for the 1 to 1
 * video communication.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class CallMediaPipeline {

	private MediaPipeline pipeline;
	private WebRtcEndpoint providerWebRtcEp;
	private WebRtcEndpoint alexaWebRtcEp;

	public CallMediaPipeline(KurentoClient kurento) {
		try {
			this.pipeline = kurento.createMediaPipeline();
			this.providerWebRtcEp = new WebRtcEndpoint.Builder(pipeline).build();
			this.alexaWebRtcEp = new WebRtcEndpoint.Builder(pipeline).build();

			this.alexaWebRtcEp.connect(this.providerWebRtcEp);
		} catch (Throwable t) {
			if (this.pipeline != null) {
				pipeline.release();
			}
		}
	}

	public void release() {
		if (pipeline != null) {
			pipeline.release();
		}
	}

	public MediaPipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(MediaPipeline pipeline) {
		this.pipeline = pipeline;
	}

	public void updateAlexaWebRtcEp() {
		this.providerWebRtcEp.disconnect(this.alexaWebRtcEp);
		this.alexaWebRtcEp.disconnect(this.providerWebRtcEp);
		this.alexaWebRtcEp.release();
		this.providerWebRtcEp.release();
		// New WebRtcEp for Alexa
		this.alexaWebRtcEp = new WebRtcEndpoint.Builder(pipeline).build();
		this.providerWebRtcEp = new WebRtcEndpoint.Builder(pipeline).build();
		this.providerWebRtcEp.connect(this.alexaWebRtcEp);
		this.alexaWebRtcEp.connect(this.providerWebRtcEp);
	}

	public WebRtcEndpoint getProviderWebRtcEp() {
		return providerWebRtcEp;
	}

	public WebRtcEndpoint getAlexaWebRtcEp() {
		return alexaWebRtcEp;
	}

}
