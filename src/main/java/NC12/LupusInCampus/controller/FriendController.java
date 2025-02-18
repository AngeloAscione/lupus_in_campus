package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dto.friend.AddFriendRequest;
import NC12.LupusInCampus.model.FriendRequest;
import NC12.LupusInCampus.model.FriendRequestPk;
import NC12.LupusInCampus.model.dao.FriendDAO;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.service.RequestService;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.model.dao.FriendRequestDAO;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import NC12.LupusInCampus.utils.clientServerComunication.NotificationCaller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("controller/friend")
public class FriendController {

    private final PlayerDAO playerDAO;
    private final FriendDAO friendDAO;
    private final FriendRequestDAO friendRequestDAO;
    private final NotificationCaller notificationCaller;
    private final MessagesResponse messagesResponse;


    @Autowired
    public FriendController(PlayerDAO playerDAO, FriendDAO friendDAO, FriendRequestDAO friendRequestDAO, NotificationCaller notificationCaller, MessagesResponse messagesResponse) {
        this.playerDAO = playerDAO;
        this.friendDAO = friendDAO;
        this.friendRequestDAO = friendRequestDAO;
        this.notificationCaller = notificationCaller;
        this.messagesResponse = messagesResponse;
    }

    @GetMapping("/get-friends-list")
    public ResponseEntity<?> getFriends(HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);
        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        Player player = (Player) session.getAttribute("player");
        player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));
        session.setAttribute("player", player);

        return messagesResponse.createResponse(endpoint, SuccessMessages.FRIENDS_LOADED, player.getFriendsList());
    }

    @DeleteMapping("/remove-friend")
    public ResponseEntity<?> removeFriend(@RequestParam String id, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        Player player = (Player) session.getAttribute("player");
        player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));
        List<Player> friends = player.getFriendsList();

        int idToRemove = Integer.parseInt(id);
        Player friendToRemove = playerDAO.findPlayerById(idToRemove);

        if (!friends.contains(friendToRemove))
            return messagesResponse.createResponse(endpoint, ErrorMessages.FRIEND_NOT_DELETED);

        friendDAO.removeFriendById(player.getId(), friendToRemove.getId());
        friendDAO.removeFriendById(friendToRemove.getId(), player.getId());
        friends.remove(friendToRemove);

        return messagesResponse.createResponse(endpoint, SuccessMessages.FRIEND_DELETED, friends);
    }


    @PostMapping("/send-friend-request")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, String> params, HttpSession session, HttpServletRequest request) {
        String endpoint = RequestService.getEndpoint(request);
        LoggerUtil.logInfo(params.get("friendId"));

        int friendId = Integer.parseInt(params.get("friendId"));

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        if (playerDAO.findPlayerById(friendId) == null)
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_FOUND);

        Player player = (Player) session.getAttribute("player");

        List<Player> friend = friendDAO.findFriendsByPlayerId(player.getId());
        Player receiver = playerDAO.findPlayerById(friendId);
        if (friend.contains(receiver))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_ALREADY_FRIEND);

        FriendRequestPk friendRequestPk = new FriendRequestPk();
        friendRequestPk.setSenderId(player.getId());
        friendRequestPk.setReceiverId(Integer.parseInt(params.get("friendId")));

        if (friendRequestDAO.existsFriendRequestByFriendRequestPk(friendRequestPk))
            return messagesResponse.createResponse(endpoint, ErrorMessages.FRIEND_REQUEST_ALREADY_SENT);

        saveFriendRequest(player, params.get("friendId"));

        LoggerUtil.logInfo("<- Risposta send friend request: " + player);
        //chiamata a controller/notification/send con parametri in POST
        return notificationCaller.sendNotificationWebClient(params.get("friendId"), "Richiesta di amicizia", player);

    }

    @PutMapping("/friend-request-result")
    public ResponseEntity<?> friendRequestResult(@RequestBody AddFriendRequest addFriendRequest, HttpSession session, HttpServletRequest request) {
        String endpoint = RequestService.getEndpoint(request);
        LoggerUtil.logInfo(addFriendRequest.toString());

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        Player player = (Player) session.getAttribute("player");

        FriendRequestPk friendRequestPk = new FriendRequestPk();
        friendRequestPk.setSenderId(Integer.parseInt(addFriendRequest.getSenderId()));
        friendRequestPk.setReceiverId(Integer.parseInt(addFriendRequest.getReceiverId()));

        if (!friendRequestDAO.existsFriendRequestByFriendRequestPk(friendRequestPk))
            return messagesResponse.createResponse(endpoint, ErrorMessages.FRIEND_REQUEST_NOT_FOUND);

        FriendRequest friendRequest = friendRequestDAO.findByFriendRequestPk(friendRequestPk);

        //the friend request does not belong to him
        if (friendRequest.getReceiverId() != player.getId())
            return messagesResponse.createResponse(endpoint, ErrorMessages.UNAUTHORIZED_OPERATION);

        return switch (addFriendRequest.getOperation()) {
            case "accepted" -> requestAccepted(friendRequest, endpoint);
            case "rejected" -> requestRejected(friendRequest, endpoint);
            default -> messagesResponse.createResponse(endpoint, ErrorMessages.INCORRECT_OPERATION);
        };
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query, HttpSession session, HttpServletRequest request) {
        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        List<Player> players = playerDAO.findPlayersByNicknameContainingIgnoreCase(query);

        players.sort(Comparator.comparing(Player::getNickname, String.CASE_INSENSITIVE_ORDER));

        return messagesResponse.createResponse(endpoint, SuccessMessages.SEARCH, players);
    }

    public ResponseEntity<?> requestAccepted(FriendRequest friendRequest, String endpoint){

        LoggerUtil.logInfo("Richiesta di amicizia accettata");
        int senderId = friendRequest.getSenderId();
        int receiverId = friendRequest.getReceiverId();

        friendDAO.addFriend(senderId, receiverId);
        friendDAO.addFriend(receiverId, senderId);

        friendRequestDAO.delete(friendRequest);

        FriendRequestPk revertFriendRequestPk = new FriendRequestPk();
        revertFriendRequestPk.setSenderId(receiverId);
        revertFriendRequestPk.setReceiverId(senderId);

        if (friendRequestDAO.existsFriendRequestByFriendRequestPk(revertFriendRequestPk)){
            FriendRequest revertFriendRequest = friendRequestDAO.findByFriendRequestPk(revertFriendRequestPk);
            friendRequestDAO.delete(revertFriendRequest);
        }

        return messagesResponse.createResponse(endpoint, SuccessMessages.FRIEND_ADDED);
    }

    public ResponseEntity<?> requestRejected(FriendRequest friendRequest, String endpoint){

        LoggerUtil.logInfo("Richiesta di amicizia rifiutata");
        friendRequestDAO.delete(friendRequest);

        return messagesResponse.createResponse(endpoint, SuccessMessages.FRIEND_REQUEST_REJECTED);
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
