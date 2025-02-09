package NC12.LupusInCampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "device")
public class Device {

    @EmbeddedId
    private DevicePk devicePk;

    public Device() {
        this.devicePk = new DevicePk();
    }

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
