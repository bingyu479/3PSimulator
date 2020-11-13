package org.kurento.tutorial.one2onecall.data;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class InitiateSessionRequest {

    private String userName;

    private String roomName;

    @Nullable
    private String sdpOffer;

    public String getUserName() {
        return userName;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getSdpOffer() {
        return sdpOffer;
    }
}
