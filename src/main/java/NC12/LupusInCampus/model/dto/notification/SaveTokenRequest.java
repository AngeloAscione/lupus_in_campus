package NC12.LupusInCampus.model.dto.notification;

public class SaveTokenRequest {

    private int playerId;
    private String token;

    public SaveTokenRequest(int playerId, String token) {
        this.playerId = playerId;
        this.token = token;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "SaveTokenRequest{" +
                "playerId=" + playerId +
                ", token='" + token + '\'' +
                '}';
    }
}
