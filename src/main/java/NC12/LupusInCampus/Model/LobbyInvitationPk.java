package NC12.LupusInCampus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LobbyInvitationPk {

    @Column(name = "giocatoreInviante")
    private int sendingPlayerId;

    @Column(name = "giocatoreInvitato")
    private int invitedPlayerId;

    public int getSendingPlayerId() {
        return sendingPlayerId;
    }

    public void setSendingPlayerId(int sendingPlayerId) {
        this.sendingPlayerId = sendingPlayerId;
    }

    public int getInvitedPlayerId() {
        return invitedPlayerId;
    }

    public void setInvitedPlayerId(int invitedPlayerId) {
        this.invitedPlayerId = invitedPlayerId;
    }
}
