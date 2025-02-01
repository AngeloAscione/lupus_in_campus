package NC12.LupusInCampus.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "richiestaamicizia")
public class FriendRequest {

    @Id
    @Column(name = "giocatoreMittente")
    private int senderId;

    @Id
    @Column(name = "giocatoreDestinatario")
    private int receiverId;

    @Column(name = "dataRichiesta")
    private LocalDateTime requestDate;

    public FriendRequest() {}

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getReceiverId() {
        return receiverId;
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
                "senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", requestDate=" + requestDate +
                '}';
    }
}
