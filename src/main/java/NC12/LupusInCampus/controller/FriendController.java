package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.FriendRequest;
import NC12.LupusInCampus.model.dao.FriendDAO;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.utils.clientServerComunication.MessageResponse;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.model.dao.FriendRequestDAO;
import NC12.LupusInCampus.utils.clientServerComunication.WebClientNotification;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("controller/friend")
public class FriendController {

    private final PlayerDAO playerDAO;
    private final FriendDAO friendDAO;
    private final FriendRequestDAO friendRequestDAO;


    @Autowired
    public FriendController(PlayerDAO playerDAO, FriendDAO friendDAO, FriendRequestDAO friendRequestDAO) {
        this.playerDAO = playerDAO;
        this.friendDAO = friendDAO;
        this.friendRequestDAO = friendRequestDAO;
    }

    @GetMapping("")
    public ResponseEntity<?> getFriends(HttpSession session) {

        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
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

        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
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


    @GetMapping("/send-friend-request")
    public ResponseEntity<?> sendFriendRequest(@RequestParam String idFriend, HttpSession session) {
        if (!Session.sessionIsActive(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponse(
                        ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                        ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
                )
            );
        }

        Player player = (Player) session.getAttribute("player");

        saveFriendRequest(player, idFriend);

        return WebClientNotification.sendNotificationWebClient(idFriend,"Richiesta di amicizia");
    }



    @GetMapping("/add-friend")
    public ResponseEntity<?> addFriend(@RequestParam FriendRequest friendRequest, @RequestParam String operation) {

        return switch (operation) {
            case "accepted" -> requestAccepted(friendRequest);
            case "rejected" -> requestRejected(friendRequest);
            default -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("operazione non corretta");
        };

    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query, HttpSession session) {
        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
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

    public ResponseEntity<?> requestAccepted(FriendRequest friendRequest){

        int senderId = friendRequest.getSenderId();
        int receiverId = friendRequest.getReceiverId();

        if (!friendRequestDAO.existsFriendRequestByFriendRequestPk(friendRequest.getFriendRequestPk())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponse(
                        -1,
                        "Richiesta di amicizia non trovata"
                )
            );
        }

        friendDAO.addFriend(senderId, receiverId);
        friendDAO.addFriend(receiverId, senderId);

        MessageResponse response = new MessageResponse(
                SuccessMessages.FRIEND_ADDED.getCode(),
                SuccessMessages.FRIEND_ADDED.getMessage()
        );

        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<?> requestRejected(FriendRequest friendRequest){

        friendRequestDAO.delete(friendRequest);

        return ResponseEntity.ok().body(
            new MessageResponse(
                    0,
                    "Richiesta di amicizia rifiutata"
            )
        );
    }

    public void saveFriendRequest(Player player, String idFriend){
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(player.getId());
        friendRequest.setReceiverId(Integer.parseInt(idFriend));
        friendRequest.setRequestDate(LocalDateTime.now());
        friendRequestDAO.save(friendRequest);
    }

}
