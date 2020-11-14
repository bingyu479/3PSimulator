package org.kurento.tutorial.one2onecall.models;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class TelehealthSessionRequest {

    private String userName;

    private String sessionId;

    @Nullable
    private String sdpOffer;

    public String getUserName() {
        return userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSdpOffer() {
        return sdpOffer;
    }
}
