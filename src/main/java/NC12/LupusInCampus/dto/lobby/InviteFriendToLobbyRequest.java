package NC12.LupusInCampus.dto.lobby;

public class InviteFriendToLobbyRequest {

    private int friendId;
    private int codeLobby;

    public InviteFriendToLobbyRequest(int friendId, int codeLobby) {
        this.friendId = friendId;
        this.codeLobby = codeLobby;
    }

    public int getFriendId() {
        return friendId;
    }

    public int getCodeLobby() {
        return codeLobby;
    }

    @Override
    public String toString() {
        return "InviteFriendToLobbyRequest{" +
                "friendId=" + friendId +
                ", codeLobby=" + codeLobby +
                '}';
    }
}
