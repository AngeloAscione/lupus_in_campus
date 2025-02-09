package NC12.LupusInCampus.model.dto.game;

import NC12.LupusInCampus.model.Player;

public class GameActionDTO {
    private Player player;
    private String action;

    public GameActionDTO() {}

    public GameActionDTO(Player player, String action) {
        this.player = player;
        this.action = action;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
