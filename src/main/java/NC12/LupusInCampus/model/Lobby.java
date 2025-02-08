package NC12.LupusInCampus.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lobby")
public class Lobby {
    @Id
    @Column(name = "codice")
    private int code;

    @Column(name = "creatoreID", unique = true, nullable = false)
    private int creatorID;

    @Column(name = "dataCreazione", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "numGiocatoriMin", nullable = false)
    private int minNumPlayer;

    @Column(name = "numGiocatori", nullable = false)
    private int numPlayer;

    @Column(name = "numGiocatoriMax", nullable = false)
    private int maxNumPlayer;

    @Column(name = "tipo", nullable = false)
    private String type;

    @Column(name = "stato", nullable = false)
    private String state;

    public Lobby() {}

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(int creatorID) {
        this.creatorID = creatorID;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public int getMinNumPlayer() {
        return minNumPlayer;
    }

    public void setMinNumPlayer(int minNumPlayer) {
        this.minNumPlayer = minNumPlayer;
    }

    public int getNumPlayer() {
        return numPlayer;
    }

    public void setNumPlayer(int numPlayer) {
        this.numPlayer = numPlayer;
    }

    public int getMaxNumPlayer() {
        return maxNumPlayer;
    }

    public void setMaxNumPlayer(int maxNumPlayer) {
        this.maxNumPlayer = maxNumPlayer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "{\"lobby\":{" +
                "\"code\":" + code +
                ", \"creatorID\":" + creatorID +
                ", \"creationDate\":\"" + creationDate + '\"' +
                ", \"minNumPlayer\":" + minNumPlayer +
                ", \"numPlayer\":" + numPlayer +
                ", \"maxNumPlayer\":" + maxNumPlayer +
                ", \"type\":\"" + type + '\"' +
                ", \"state\":\"" + state + '\"' +
                "}}";
    }
}
