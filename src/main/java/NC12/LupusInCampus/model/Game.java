package NC12.LupusInCampus.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "partita")
public class Game {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "idCreatoreLobby", nullable = false)
    private int creatorId;

    @Column(name = "dataPartita", nullable = false)
    private LocalDateTime gameDate;

    @Column(name = "vincitore")
    private int winningPlayerId;

    @Transient
    private List<Player> participants;

    public Game(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDateTime gameDate) {
        this.gameDate = gameDate;
    }

    public int getWinningPlayerId() {
        return winningPlayerId;
    }

    public void setWinningPlayerId(int winningPlayerId) {
        this.winningPlayerId = winningPlayerId;
    }

    public List<Player> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Player> participants) {
        this.participants = participants;
    }


    @Override
    public String toString() {
        return "{\"Game\":{" +
                "\"id\":" + id + '"' +
                ", \"creatorId\":\"" + creatorId +  '"' +
                ", \"gameDate\":\"" + gameDate + '"' +
                ", \"winningPlayerId\":\"" + winningPlayerId + '"' +
                ", \"participants\":" + participants +
                "}}";
    }
}
