package NC12.LupusInCampus.model.dto.game;

public class GameUpdateMessage {

    private String type;
    private String data;
    private String lobbyCode;

    public GameUpdateMessage() {
    }

    public GameUpdateMessage(String type, String data, String lobbyCode) {
        this.type = type;
        this.data = data;
        this.lobbyCode = lobbyCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getLobbyCode() {
        return lobbyCode;
    }

    public void setLobbyCode(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }
}
