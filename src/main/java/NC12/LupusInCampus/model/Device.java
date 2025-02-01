package NC12.LupusInCampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "device")
public class Device {

    @EmbeddedId
    DevicePk devicePk;

    public Device() {}

    public DevicePk getDevicePk() {
        return devicePk;
    }

    public String getDeviceToken() {
        return this.devicePk.getDeviceToken();
    }

    public void setDeviceToken(String deviceToken) {
        this.devicePk.setDeviceToken(deviceToken);
    }

    public int getPlayerID() {
        return this.devicePk.getPlayerID();
    }

    public void setPlayerID(int playerID) {
        this.devicePk.setPlayerID(playerID);
    }
}
