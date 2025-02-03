package NC12.LupusInCampus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class FriendRequestPk {

    @Column(name = "giocatoreMittente")
    private int senderId;

    @Column(name = "giocatoreDestinatario")
    private int receiverId;

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendRequestPk that = (FriendRequestPk) o;
        return senderId == that.senderId && receiverId == that.receiverId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId);
    }
}
