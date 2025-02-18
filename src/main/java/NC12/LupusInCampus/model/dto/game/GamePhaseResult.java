package NC12.LupusInCampus.model.dto.game;

public class GamePhaseResult {
    private String phase;
    private String voted_player;

    public GamePhaseResult() {
    }

    public GamePhaseResult(String phase, String voted_player) {
        this.phase = phase;
        this.voted_player = voted_player;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getVoted_player() {
        return voted_player;
    }

    public void setVoted_player(String voted_player) {
        this.voted_player = voted_player;
    }

    @Override
    public String toString() {
        return "GamePhaseResult{" +
                "phase='" + phase + '\'' +
                ", voted_player='" + voted_player + '\'' +
                '}';
    }
}
