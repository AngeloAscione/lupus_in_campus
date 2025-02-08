package NC12.LupusInCampus.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invito")
public class LobbyInvitation {

    @EmbeddedId
    LobbyInvitationPk lobbyInvitationPk;

    @Column(name = "dataInvito")
    private LocalDateTime dataInvitation;

    public LobbyInvitation() {
        this.lobbyInvitationPk = new LobbyInvitationPk();
    }

    public void setDataInvitation(LocalDateTime dataInvitation) {
        this.dataInvitation = dataInvitation;
    }

    public void setInvitedPlayerId(int invitedPlayerId) {
        this.lobbyInvitationPk.setInvitedPlayerId(invitedPlayerId);
    }

    public void setSendingPlayerId(int sendingPlayerId) {
        this.lobbyInvitationPk.setSendingPlayerId(sendingPlayerId);
    }

    public int getSendingPlayerId() {
        return this.lobbyInvitationPk.getSendingPlayerId();
    }

    public LobbyInvitationPk getLobbyInvitationPk() {
        return lobbyInvitationPk;
    }

    public void setLobbyInvitationPk(LobbyInvitationPk lobbyInvitationPk) {
        this.lobbyInvitationPk = lobbyInvitationPk;
    }

    public LocalDateTime getDataInvitation() {
        return dataInvitation;
    }

    public int getInvitedPlayerId() {
        return this.lobbyInvitationPk.getInvitedPlayerId();
    }

    @Override
    public String toString() {
        return "{\"LobbyInvitation\":{" +
                "\"sendingPlayerId\":" + this.lobbyInvitationPk.getSendingPlayerId() +
                ", \"invitedPlayerId\":" + this.lobbyInvitationPk.getInvitedPlayerId() +
                ", \"dataInvitation\":\""  + this.dataInvitation + '\"' +
                "}}";
    }
}
