package NC12.LupusInCampus.model.dto.lobby;

public class LobbyUpdate {
    private String type;
    private String player;

    public LobbyUpdate() {
    }

    public LobbyUpdate(String player, String type) {
        this.player = player;
        this.type = type;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
