package NC12.LupusInCampus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LobbyInvitationPk that = (LobbyInvitationPk) o;
        return sendingPlayerId == that.sendingPlayerId && invitedPlayerId == that.invitedPlayerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sendingPlayerId, invitedPlayerId);
    }
}
