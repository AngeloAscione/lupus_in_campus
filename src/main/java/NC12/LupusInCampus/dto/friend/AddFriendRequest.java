package NC12.LupusInCampus.dto.friend;

public class AddFriendRequest {
    private String myId;
    private String friendId;
    private String operation;

    public AddFriendRequest(String myId, String friendId, String operation) {
        this.myId = myId;
        this.friendId = friendId;
        this.operation = operation;
    }

    public String getMyId() {
        return myId;
    }

    public String getFriendId() {
        return friendId;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return "AddFriendRequest{" +
                "myId='" + myId + '\'' +
                ", friendId='" + friendId + '\'' +
                ", operation='" + operation + '\'' +
                '}';
    }
}
