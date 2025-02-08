package NC12.LupusInCampus.dto.friend;

public class AddFriendRequest {
    private String senderId;
    private String receiverId;
    private String operation;

    public AddFriendRequest(String senderId, String receiverId, String operation) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.operation = operation;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return "AddFriendRequest{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", operation='" + operation + '\'' +
                '}';
    }
}
