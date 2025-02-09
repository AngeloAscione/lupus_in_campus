package NC12.LupusInCampus.model.dto.lobby;


public class ModifyLobbyRequest {
    private int codeLobby;
    private int minNumPlayer;
    private int maxNumPlayer;

    public ModifyLobbyRequest(int codeLobby, int minNumPlayer, int maxNumPlayer) {
        this.codeLobby = codeLobby;
        this.minNumPlayer = minNumPlayer;
        this.maxNumPlayer = maxNumPlayer;
    }

    public int getCodeLobby() {
        return codeLobby;
    }

    public int getMinNumPlayer() {
        return minNumPlayer;
    }

    public int getMaxNumPlayer() {
        return maxNumPlayer;
    }

    @Override
    public String toString() {
        return "ModifyLobbyRequest{" +
                "codeLobby=" + codeLobby +
                ", minNumPlayer=" + minNumPlayer +
                ", maxNumPlayer=" + maxNumPlayer +
                '}';
    }
}
