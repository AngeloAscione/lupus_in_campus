package NC12.LupusInCampus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;

@Embeddable
public class DevicePk {
    @Column(name = "giocatoreID")
    private int playerID;

    @Column(name = "deviceToken")
    private String deviceToken;


    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
