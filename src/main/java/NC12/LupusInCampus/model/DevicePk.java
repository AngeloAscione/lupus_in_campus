package NC12.LupusInCampus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevicePk devicePk = (DevicePk) o;
        return playerID == devicePk.playerID && Objects.equals(deviceToken, devicePk.deviceToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerID, deviceToken);
    }
}
