package NC12.LupusInCampus.model.dto.lobby;


public class CreateLobbyRequest {

    private int minNumPlayer;
    private int maxNumPlayer;
    private String tipo;

    public CreateLobbyRequest(int minNumPlayer, int maxNumPlayer, String tipo) {
        this.minNumPlayer = minNumPlayer;
        this.maxNumPlayer = maxNumPlayer;
        this.tipo = tipo;
    }

    public int getMinNumPlayer() {
        return minNumPlayer;
    }

    public int getMaxNumPlayer() {
        return maxNumPlayer;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return "CreateLobbyRequest{" +
                "minNumPlayer=" + minNumPlayer +
                ", maxNumPlayer=" + maxNumPlayer +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}
