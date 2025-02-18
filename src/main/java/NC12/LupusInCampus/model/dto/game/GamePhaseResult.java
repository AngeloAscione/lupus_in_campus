package NC12.LupusInCampus.model.dto.game;

public class GamePhaseResult {
    private String phase;
    private String votedPlayer;
    private String votingPlayer;

    public GamePhaseResult() {
    }

    public GamePhaseResult(String phase, String voted_player) {
        this.phase = phase;
        this.votedPlayer = voted_player;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getVotedPlayer() {
        return votedPlayer;
    }

    public void setVotedPlayer(String votedPlayer) {
        this.votedPlayer = votedPlayer;
    }

    public String getVotingPlayer() {
        return votingPlayer;
    }

    public void setVotingPlayer(String votingPlayer) {
        this.votingPlayer = votingPlayer;
    }

    @Override
    public String toString() {
        return "GamePhaseResult{" +
                "phase='" + phase + '\'' +
                ", voted_player='" + votedPlayer + '\'' +
                '}';
    }

}
