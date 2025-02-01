package NC12.LupusInCampus.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "invito")
public class LobbyInvitation {

    @Id
    @Column(name = "giocatoreInviante")
    private int sendingPlayerId;

    @Id
    @Column(name = "giocatoreInvitato")
    private int invitedPlayerId;

    @Column(name = "dataInvito")
    private LocalDateTime dataInvitation;

    public LobbyInvitation() {}

    public void setDataInvitation(LocalDateTime dataInvitation) {
        this.dataInvitation = dataInvitation;
    }

    public void setInvitedPlayerId(int invitedPlayerId) {
        this.invitedPlayerId = invitedPlayerId;
    }

    public void setSendingPlayerId(int sendingPlayerId) {
        this.sendingPlayerId = sendingPlayerId;
    }

    public int getSendingPlayerId() {
        return sendingPlayerId;
    }

    public LocalDateTime getDataInvitation() {
        return dataInvitation;
    }

    public int getInvitedPlayerId() {
        return invitedPlayerId;
    }

    @Override
    public String toString() {
        return "LobbyInvitation{" +
                "sendingPlayerId=" + sendingPlayerId +
                ", invitedPlayerId=" + invitedPlayerId +
                ", dataInvitation=" + dataInvitation +
                '}';
    }
}
