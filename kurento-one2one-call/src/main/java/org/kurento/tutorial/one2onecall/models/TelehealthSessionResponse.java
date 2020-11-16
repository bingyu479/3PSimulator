package org.kurento.tutorial.one2onecall.models;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class TelehealthSessionResponse {

    private String userName;

    private String sessionId;

    @Nullable
    private String sdpOffer;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Nullable
    public String getSdpOffer() {
        return sdpOffer;
    }

    public void setSdpOffer(@Nullable String sdpOffer) {
        this.sdpOffer = sdpOffer;
    }
}
