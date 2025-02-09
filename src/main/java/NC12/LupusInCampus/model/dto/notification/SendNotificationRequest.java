package NC12.LupusInCampus.model.dto.notification;

public class SendNotificationRequest {

    private int receiverId;
    private String message;

    public SendNotificationRequest(int receiverId, String message) {
        this.receiverId = receiverId;
        this.message = message;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SendNotificationRequest{" +
                "receiverId=" + receiverId +
                ", message='" + message + '\'' +
                '}';
    }
}

