package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.FriendDAO;
import NC12.LupusInCampus.Model.DAO.PlayerDAO;
import NC12.LupusInCampus.Model.Enums.ErrorMessages;
import NC12.LupusInCampus.Model.Enums.SuccessMessages;
import NC12.LupusInCampus.Model.Player;
import NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer.MessageResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("controller/friend")
public class FriendController {

    private final PlayerDAO playerDAO;
    private final FriendDAO friendDAO;

    @Autowired
    public FriendController(PlayerDAO playerDAO, FriendDAO friendDAO) {
        this.playerDAO = playerDAO;
        this.friendDAO = friendDAO;
    }

    @GetMapping("")
    public ResponseEntity<?> getFriends(HttpSession session) {

        if (!sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");
        player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));
        session.setAttribute("player", player);

        MessageResponse response = new MessageResponse(
            SuccessMessages.FRIEND_LOADED.getCode(),
            SuccessMessages.FRIEND_LOADED.getMessage(),
            player.getFriendsList()
        );
        return ResponseEntity.ok().body(response);


    }

    @GetMapping("/remove-friend")
    public ResponseEntity<?> removeFriend(@RequestParam String id, HttpSession session) {

        if (!sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");
        player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));
        List<Player> friends = player.getFriendsList();

        int idToRemove = Integer.parseInt(id);
        Player friendToRemove = playerDAO.findPlayerById(idToRemove);

        if (!friends.contains(friendToRemove)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new MessageResponse(
                    ErrorMessages.FRIEND_NOT_DELETED.getCode(),
                    ErrorMessages.FRIEND_NOT_DELETED.getMessage()
            )
        );

        friendDAO.removeFriendById(player.getId(), friendToRemove.getId());

        MessageResponse response = new MessageResponse(
                SuccessMessages.FRIEND_DELETED.getCode(),
                SuccessMessages.FRIEND_DELETED.getMessage()
        );
        return ResponseEntity.ok().body(response);

    }

    /* TODO function to send a friend request, we need to see how to do notifications
    @GetMapping("/send-friend-request")
    public ResponseEntity<?> sendFriendRequest(@RequestParam String idOwner, @RequestParam String idFriend, HttpSession session){}
    */

    //TODO modify after doing sendFriendRequest function
    @GetMapping("/add-friend")
    public ResponseEntity<?> addFriend(@RequestParam String id, HttpSession session) {

        if (!sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");

        int idToAdd = Integer.parseInt(id);
        Player friendToAdd = playerDAO.findPlayerById(idToAdd);

        if (friendToAdd == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
             new MessageResponse(
                    ErrorMessages.PLAYER_NOT_FOUND.getCode(),
                    ErrorMessages.PLAYER_NOT_FOUND.getMessage()
            )
        );

        List<Player> friends = friendDAO.findFriendsByPlayerId(player.getId());

        if (friends.contains(friendToAdd)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.FRIEND_ALREADY_ADDED.getCode(),
                    ErrorMessages.FRIEND_ALREADY_ADDED.getMessage()
            )
        );

        friendDAO.addFriend(player.getId(), friendToAdd.getId());
        player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));
        session.setAttribute("player", player);

        MessageResponse response = new MessageResponse(
                SuccessMessages.FRIEND_ADDED.getCode(),
                SuccessMessages.FRIEND_ADDED.getMessage()
        );

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query, HttpSession session) {
        if (!sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        List<Player> players = playerDAO.findPlayersByNicknameContainingIgnoreCase(query);

        return ResponseEntity.ok().body(
            new MessageResponse(
                    SuccessMessages.SEARCH.getCode(),
                    SuccessMessages.SEARCH.getMessage(),
                    players
            )
        );

    }

    public boolean sessionIsActive(HttpSession session) {
        return session.getAttribute("player") != null;
    }
}
