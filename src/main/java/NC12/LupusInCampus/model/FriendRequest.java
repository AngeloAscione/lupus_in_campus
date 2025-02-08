package NC12.LupusInCampus.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "richiestaamicizia")
public class FriendRequest {

    @EmbeddedId
    private FriendRequestPk friendRequestPk;

    @Column(name = "dataRichiesta")
    private LocalDateTime requestDate;

    public FriendRequest() {
        this.friendRequestPk = new FriendRequestPk();
    }

    public void setSenderId(int senderId) {
        this.friendRequestPk.setSenderId(senderId);
    }

    public int getSenderId() {
        return this.friendRequestPk.getSenderId();
    }

    public void setReceiverId(int receiverId) {
        this.friendRequestPk.setReceiverId(receiverId);
    }

    public int getReceiverId() {
        return this.friendRequestPk.getReceiverId();
    }

    public FriendRequestPk getFriendRequestPk() {
        return this.friendRequestPk;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "senderId=" + this.friendRequestPk.getSenderId() +
                ", receiverId=" + this.friendRequestPk.getReceiverId() +
                ", requestDate=" + requestDate +
                '}';
    }
}
