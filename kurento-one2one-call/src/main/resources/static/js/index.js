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

var ws = new WebSocket('wss://' + location.host + '/call');
var videoInput;
var videoOutput;
var webRtcPeer;
var response;
var callerMessage;
var from;

var registerName = null;
var registerState = null;
const NOT_REGISTERED = 0;
const REGISTERING = 1;
const REGISTERED = 2;

function setRegisterState(nextState) {
	switch (nextState) {
	case NOT_REGISTERED:
		enableButton('#register', 'register()');
		setCallState(NO_CALL);
		break;
	case REGISTERING:
		disableButton('#register');
		break;
	case REGISTERED:
		disableButton('#register');
		setCallState(NO_CALL);
		break;
	default:
		return;
	}
	registerState = nextState;
}

var callState = null;
const NO_CALL = 0;
const PROCESSING_CALL = 1;
const IN_CALL = 2;

function setCallState(nextState) {
	switch (nextState) {
	case NO_CALL:
		enableButton('#joinAsDoctor', 'joinAsDoctor()');
		enableButton('#joinAsAlexa', 'joinAsAlexa()');
		enableButton('#toggleMute', 'toggleMute()');
        enableButton('#toggleVideo', 'toggleVideo()');
		disableButton('#terminate');
		disableButton('#play');
		break;
	case PROCESSING_CALL:
		disableButton('#joinAsDoctor');
		disableButton('#joinAsAlexa');
		enableButton('#toggleMute', 'toggleMute()');
        enableButton('#toggleVideo', 'toggleVideo()');
		disableButton('#terminate');
		disableButton('#play');
		break;
	case IN_CALL:
		disableButton('#joinAsDoctor');
		disableButton('#joinAsAlexa');
		enableButton('#toggleMute', 'toggleMute()');
		enableButton('#toggleVideo', 'toggleVideo()');
		enableButton('#terminate', 'stop()');
		disableButton('#play');
		break;
	default:
		return;
	}
	callState = nextState;
}

window.onload = function() {
	console = new Console();
	setRegisterState(NOT_REGISTERED);
	var drag = new Draggabilly(document.getElementById('videoSmall'));
	videoInput = document.getElementById('videoInput');
	videoOutput = document.getElementById('videoOutput');
	document.getElementById('name').focus();
}

window.onbeforeunload = function() {
	ws.close();
}

ws.onmessage = function(message) {
	var parsedMessage = JSON.parse(message.data);
	console.info('Received message: ' + message.data);

	switch (parsedMessage.id) {
	case 'registerResponse':
		registerResponse(parsedMessage);
		break;
	case 'iceCandidate':
        //Received remote peer's candidate
		webRtcPeer.addIceCandidate(parsedMessage.candidate, function(error) {
			if (error)
				return console.error('Error adding candidate: ' + error);
		});
		break;
	case 'startCommunication':
        startCommunication(parsedMessage);
        break;
    case 'updatedSdpOffer':
        reNegotiateWithOffer(parsedMessage);
        break;
    case 'stopCommunication':
        console.info('Communication ended by remote peer');
        stop(true);
        break;
	default:
		console.error('Unrecognized message', parsedMessage);
	}
}

function registerResponse(message) {
	if (message.response == 'accepted') {
		setRegisterState(REGISTERED);
	} else {
		setRegisterState(NOT_REGISTERED);
		var errorMessage = message.message ? message.message
				: 'Unknown reason for register rejection.';
		console.log(errorMessage);
		alert('Error registering user. See console for further information.');
	}
}

function register() {
	var name = document.getElementById('name').value;
	if (name == '') {
		window.alert('You must insert your user name');
		return;
	}
	setRegisterState(REGISTERING);

	var message = {
		id : 'register',
		name : name
	};
	sendMessage(message);
	document.getElementById('room').focus();
}

function joinAsDoctor() {
	if (document.getElementById('room').value == '') {
		window.alert('You must specify the room name');
		return;
	}
	setCallState(PROCESSING_CALL);
	showSpinner(videoInput, videoOutput);

	var options = {
		localVideo : videoInput,
		remoteVideo : videoOutput,
		onicecandidate : onIceCandidate,
		onerror : onError
	}
	webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
			function(error) {
				if (error) {
					return console.error(error);
				}
				webRtcPeer.generateOffer(onOfferCall);
			});
}

function toggleMute() {
    var text = $('#toggleMute').text();
    if (text == 'Mute') {
        webRtcPeer.audioEnabled = false;
        $("#toggleMute > span").text("Unmute");
        $('#toggleMute > i').toggleClass('fa-microphone').toggleClass('fa-microphone-slash');
    } else if (text == 'Unmute') {
        webRtcPeer.audioEnabled = true;
        $("#toggleMute > span").text("Mute");
        $('#toggleMute > i').toggleClass('fa-microphone-slash').toggleClass('fa-microphone');
    }
}

function toggleVideo() {
    var text = $('#toggleVideo').text();
    if (text == 'Video Off') {
        webRtcPeer.videoEnabled = false;
        $("#toggleVideo > span").text("Video On");
        $('#toggleVideo > i').toggleClass('fa-video').toggleClass('fa-video-slash');
    } else if (text == 'Video On') {
        webRtcPeer.videoEnabled = true;
        $("#toggleVideo > span").text("Video Off");
        $('#toggleVideo > i').toggleClass('fa-video-slash').toggleClass('fa-video');
    }
}

// When doctor receive the SDP answer from App server (from calleeWebRtcEp)
function startCommunication(message) {
	setCallState(IN_CALL);
	webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
		if (error)
			return console.error(error);
	});
}

function onOfferCall(error, offerSdp) {
	if (error)
		return console.error('Error generating the offer');
	console.log('Sending SDP offer for doctor via WebSocket');
	var message = {
		id : 'providerJoinSession',
		provider : document.getElementById('name').value,
		room : document.getElementById('room').value,
		sdpOffer : offerSdp
	};
	sendMessage(message);
}

function disableVideoOffer(error, offerSdp) {
    if (error)
        return console.error('Error generating the offer');
    console.log('Sending an updated SDP offer for doctor via WebSocket to disable the video');
    var message = {
        id : 'disableVideoOffer',
        provider : document.getElementById('name').value,
        room : document.getElementById('room').value,
        sdpOffer : offerSdp
    };
    sendMessage(message);
}

function muteAudioOffer(error, offerSdp) {
    if (error)
        return console.error('Error generating the offer');
    console.log('Sending an updated SDP offer for doctor via WebSocket to mute the audio');
    var message = {
        id : 'muteProviderOffer',
        provider : document.getElementById('name').value,
        room : document.getElementById('room').value,
        sdpOffer : offerSdp
    };
    sendMessage(message);
}

function joinAsAlexa() {
	if (document.getElementById('room').value == '') {
		window.alert('You must specify the room name');
		return;
	}
	setCallState(PROCESSING_CALL);
	showSpinner(videoInput, videoOutput);

	var options = {
		localVideo : videoInput,
		remoteVideo : videoOutput,
		onicecandidate : onIceCandidate,
		onerror : onError
	}
	webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
			function(error) {
				if (error) {
					return console.error(error);
				}
				webRtcPeer.generateOffer(onOfferHttpCall);
			});
}

function onOfferHttpCall(error, offerSdp) {
    if (error)
    	return console.error('Error generating the offer');
    console.log('Sending SDP offer for Alexa via InitiateSession API call');

    // Call InitiateSession API
//    const data = {
//       userName: document.getElementById('name').value,
//       sessionId: document.getElementById('room').value,
//       sdpOffer: offerSdp,
//       iceServers: [{
//                      url: 'turn:3psimulator-env.eba-j3qpx3j8.us-west-2.elasticbeanstalk.com:4172?transport=udp',
//                      username: '1605127832:tk9fafcdbf-404c-4bda-96d2-402dd262fc0a-us-east-1_1605122432579_0',
//                      credential: 'qYmEjPp03svJDe66GWG4v2q2fGk='
//                  }, {
//                      url: 'turns:3psimulator-env.eba-j3qpx3j8.us-west-2.elasticbeanstalk.com:443?transport=tcp',
//                      username: '1605127832:tk9fafcdbf-404c-4bda-96d2-402dd262fc0a-us-east-1_1605122432579_0',
//                      credential: 'qYmEjPp03svJDe66GWG4v2q2fGk='
//                  }, {
//                      url: 'stun:3psimulator-env.eba-j3qpx3j8.us-west-2.elasticbeanstalk.com:4172',
//                      username: null,
//                      credential: null
//                  }]
//    };

    //For Alexa in web browser
    const data = {
       userName: document.getElementById('name').value,
       sessionId: document.getElementById('room').value,
       sdpOffer: offerSdp
    };

    console.log('data is');
    console.log('data is {}' + JSON.stringify(data));
    fetch('/alexa/telehealth/session/initiate', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });
}

function reNegotiateWithOffer(message) {

    var options = {
		localVideo : videoInput,
		remoteVideo : videoOutput,
		onicecandidate : onIceCandidate,
		onerror : onError
	}
    webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
        function(error) {
            if (error) {
                return console.error(error);
            }
            webRtcPeer.processOffer(message.sdpOffer, onReNegotiation);
        });
}

function onReNegotiation(error, sdpAnswer) {
    if (error)
    		return console.error('Error generating the offer');
    console.log('Provider: Sending SDP answer for re-negotiation');
    var message = {
        id : 'updatedSdpAnswer',
        provider : document.getElementById('name').value,
        room : document.getElementById('room').value,
        sdpAnswer : sdpAnswer
    };
    sendMessage(message);
}

function stop(message) {
	setCallState(NO_CALL);
	if (webRtcPeer) {
		webRtcPeer.dispose();
		webRtcPeer = null;

		if (!message) {
			var message = {
				id : 'stop',
				room : document.getElementById('room').value
			}
			sendMessage(message);
		}
	}
	hideSpinner(videoInput, videoOutput);
}

function onError() {
	setCallState(NO_CALL);
}

function onIceCandidate(candidate) {
	console.log("Local candidate" + JSON.stringify(candidate));

	var message = {
		id : 'onIceCandidate',
		candidate : candidate
	};
	sendMessage(message);
}

function sendMessage(message) {
	var jsonMessage = JSON.stringify(message);
	console.log('Sending message: ' + jsonMessage);
	ws.send(jsonMessage);
}

function showSpinner() {
	for (var i = 0; i < arguments.length; i++) {
		arguments[i].poster = './img/transparent-1px.png';
		arguments[i].style.background = 'center transparent url("./img/spinner.gif") no-repeat';
	}
}

function hideSpinner() {
	for (var i = 0; i < arguments.length; i++) {
		arguments[i].src = '';
		arguments[i].poster = './img/webrtc.png';
		arguments[i].style.background = '';
	}
}

function disableButton(id) {
	$(id).attr('disabled', true);
	$(id).removeAttr('onclick');
}

function enableButton(id, functionName) {
	$(id).attr('disabled', false);
	$(id).attr('onclick', functionName);
}

/**
 * Lightbox utility (to display media pipeline image in a modal dialog)
 */
$(document).delegate('*[data-toggle="lightbox"]', 'click', function(event) {
	event.preventDefault();
	$(this).ekkoLightbox();
});
