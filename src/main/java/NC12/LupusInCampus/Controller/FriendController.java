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
        Player player = (Player) session.getAttribute("player");

        if (player != null) {
            player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));
            session.setAttribute("player", player);

            MessageResponse response = new MessageResponse(
                    SuccessMessages.FRIEND_LOADED.getCode(),
                    SuccessMessages.FRIEND_LOADED.getMessage(),
                    player.getFriendsList()
            );
            return ResponseEntity.ok().body(response);

        }else {
            MessageResponse response = new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/remove-friend")
    public ResponseEntity<?> removeFriend(@RequestParam String id, HttpSession session) {

        Player player = (Player) session.getAttribute("player");
        MessageResponse response;
        if (player != null) {
            player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));

            List<Player> friends = player.getFriendsList();

            System.out.println(friends);

            int idToRemove = Integer.parseInt(id);
            Player friendToRemove = playerDAO.findPlayerById(idToRemove);

            if (friends.contains(friendToRemove)) {
                friendDAO.removeFriendById(player.getId(), friendToRemove.getId());

                response = new MessageResponse(
                        SuccessMessages.FRIEND_DELETED.getCode(),
                        SuccessMessages.FRIEND_DELETED.getMessage()
                );
                return ResponseEntity.ok().body(response);

            }else {
                response = new MessageResponse(
                        ErrorMessages.FRIEND_NOT_DELETED.getCode(),
                        ErrorMessages.FRIEND_NOT_DELETED.getMessage()
                );
            }
        }else {
            response = new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            );
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @GetMapping("/add-friend")
    public ResponseEntity<?> addFriend(@RequestParam String id, HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        MessageResponse response;
        if (player != null) {
            int idToAdd = Integer.parseInt(id);
            Player friendToAdd = playerDAO.findPlayerById(idToAdd);

            List<Player> friends = friendDAO.findFriendsByPlayerId(player.getId());

            if (friendToAdd != null) {
                System.out.println(friends);
                if (!friends.contains(friendToAdd)) {

                    friendDAO.addFriend(player.getId(), friendToAdd.getId());
                    player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));
                    session.setAttribute("player", player);

                    response = new MessageResponse(
                            SuccessMessages.FRIEND_ADDED.getCode(),
                            SuccessMessages.FRIEND_ADDED.getMessage()
                    );

                    return ResponseEntity.ok().body(response);

                }else {
                    response = new MessageResponse(
                            ErrorMessages.FRIEND_ALREADY_ADDED.getCode(),
                            ErrorMessages.FRIEND_ALREADY_ADDED.getMessage()
                    );
                }

            }else {
                response = new MessageResponse(
                        ErrorMessages.PLAYER_NOT_FOUND.getCode(),
                        ErrorMessages.PLAYER_NOT_FOUND.getMessage()
                );
            }
        }else {
            response = new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            );
        }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
