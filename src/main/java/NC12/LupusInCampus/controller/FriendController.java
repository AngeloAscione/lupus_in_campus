package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.dto.friend.AddFriendRequest;
import NC12.LupusInCampus.model.FriendRequest;
import NC12.LupusInCampus.model.FriendRequestPk;
import NC12.LupusInCampus.model.dao.FriendDAO;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.clientServerComunication.MessageResponse;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.model.dao.FriendRequestDAO;
import NC12.LupusInCampus.utils.clientServerComunication.NotificationCaller;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("controller/friend")
public class FriendController {

    private final PlayerDAO playerDAO;
    private final FriendDAO friendDAO;
    private final FriendRequestDAO friendRequestDAO;
    private final NotificationCaller notificationCaller;


    @Autowired
    public FriendController(PlayerDAO playerDAO, FriendDAO friendDAO, FriendRequestDAO friendRequestDAO, NotificationCaller notificationCaller) {
        this.playerDAO = playerDAO;
        this.friendDAO = friendDAO;
        this.friendRequestDAO = friendRequestDAO;
        this.notificationCaller = notificationCaller;
    }

    @GetMapping("")
    public ResponseEntity<?> getFriends(HttpSession session) {
        LoggerUtil.logInfo("-> Ricevuta richiesta di get all friends");
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

        LoggerUtil.logInfo("<- Risposta get all friends: " + response);
        return ResponseEntity.ok().body(response);

    }

    @DeleteMapping("/remove-friend")
    public ResponseEntity<?> removeFriend(@RequestParam String id, HttpSession session) {

        LoggerUtil.logInfo("-> Ricevuta richiesta di remove friend");
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

        LoggerUtil.logInfo("<- Risposta remove friend: " + response);
        return ResponseEntity.ok().body(response);

    }


    @PostMapping("/send-friend-request")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, String> params, HttpSession session) {
        LoggerUtil.logInfo("-> Ricevuta richiesta di send friend request");
        LoggerUtil.logInfo(params.get("friendId"));
        if (!Session.sessionIsActive(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new MessageResponse(
                            ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                            ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
                    )
            );
        }

        Player player = (Player) session.getAttribute("player");

        saveFriendRequest(player, params.get("friendId"));

        LoggerUtil.logInfo("<- Risposta send friend request: " + player);
        //chiamata a controller/notification/send con parametri in POST
        return notificationCaller.sendNotificationWebClient(idFriend, "Richiesta di amicizia");

    }



    @PostMapping("/add-friend")
    public ResponseEntity<?> addFriend(@RequestBody AddFriendRequest addFriendRequest) {
        LoggerUtil.logInfo("-> Ricevuta richiesta di add friend");
        LoggerUtil.logInfo(addFriendRequest.toString());
        FriendRequestPk friendRequestPk = new FriendRequestPk();
        friendRequestPk.setReceiverId(Integer.parseInt(addFriendRequest.getMyId()));
        friendRequestPk.setSenderId(Integer.parseInt(addFriendRequest.getFriendId()));
        FriendRequest friendRequest = friendRequestDAO.findByFriendRequestPk(friendRequestPk);
        LoggerUtil.logInfo("<- Risposta add friend: " + friendRequest);
        return switch (addFriendRequest.getOperation()) {
            case "accepted" -> requestAccepted(friendRequest);
            case "rejected" -> requestRejected(friendRequest);
            default -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("operazione non corretta");
        };

    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query, HttpSession session) {
        LoggerUtil.logInfo("-> Ricevuta richiesta di search");
        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        List<Player> players = playerDAO.findPlayersByNicknameContainingIgnoreCase(query);

        LoggerUtil.logInfo("<- Risposta search: " + players);
        return ResponseEntity.ok().body(
            new MessageResponse(
                    SuccessMessages.SEARCH.getCode(),
                    SuccessMessages.SEARCH.getMessage(),
                    players
            )
        );

    }

    public ResponseEntity<?> requestAccepted(FriendRequest friendRequest){

        LoggerUtil.logInfo("Richiesta di amicizia accettata");
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

        LoggerUtil.logInfo("Richiesta di amicizia rifiutata");
        friendRequestDAO.delete(friendRequest);

        return ResponseEntity.ok().body(
            new MessageResponse(
                    0,
                    "Richiesta di amicizia rifiutata"
            )
        );
    }

    public void saveFriendRequest(Player player, String idFriend){
        LoggerUtil.logInfo("Salvo richiesta di amicizia");
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(player.getId());
        friendRequest.setReceiverId(Integer.parseInt(idFriend));
        friendRequest.setRequestDate(LocalDateTime.now());
        friendRequestDAO.save(friendRequest);
    }

}
