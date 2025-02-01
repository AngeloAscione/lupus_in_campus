package NC12.LupusInCampus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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
}
