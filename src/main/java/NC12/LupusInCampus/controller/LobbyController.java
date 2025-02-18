package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dto.lobby.CreateLobbyRequest;
import NC12.LupusInCampus.model.dto.lobby.InviteFriendToLobbyRequest;
import NC12.LupusInCampus.model.dto.lobby.ModifyLobbyRequest;
import NC12.LupusInCampus.model.LobbyInvitationPk;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.service.RequestService;
import NC12.LupusInCampus.service.ListPlayersLobbiesService;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.model.dao.LobbyInvitationDAO;
import NC12.LupusInCampus.model.LobbyInvitation;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import NC12.LupusInCampus.utils.clientServerComunication.NotificationCaller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("controller/lobby")
public class LobbyController {

    private static LobbyDAO lobbyDAO = null;
    private final LobbyInvitationDAO lobbyInvitationDAO;
    private final NotificationCaller notificationCaller;
    private static ListPlayersLobbiesService lobbyLists = new ListPlayersLobbiesService();
    private final MessagesResponse messagesResponse;


    @Autowired
    public LobbyController(LobbyDAO lobbyDAO_param, LobbyInvitationDAO lobbyInvitationDAO, NotificationCaller notificationCaller, MessagesResponse messagesResponse) {
        lobbyDAO = lobbyDAO_param;
        this.lobbyInvitationDAO = lobbyInvitationDAO;
        this.notificationCaller = notificationCaller;
        LobbyController.lobbyLists = lobbyLists;
        this.messagesResponse = messagesResponse;
    }

    @GetMapping("/lobby-invitations")
    public ResponseEntity<?> getLobbyInvitations(HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        Player player = (Player) session.getAttribute("player");
        List<LobbyInvitation> invitations =  lobbyInvitationDAO.findLobbyInvitationsByLobbyInvitationPkInvitedPlayerId(player.getId());

        return messagesResponse.createResponse(endpoint, SuccessMessages.LOBBIES_INIVTATION_LOADED, invitations);
    }

    //to receive a list of all active public lobbies
    @GetMapping("/active-public-lobbies")
    public ResponseEntity<?> getActivePublicLobbies(HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        if (lobbyDAO.count() == 0)
            return messagesResponse.createResponse(endpoint, ErrorMessages.PUBLIC_LOBBIES_NOT_FOUND);

        List<Lobby> lobbies = lobbyDAO.findAllByType("Pubblica"); //public

        return messagesResponse.createResponse(endpoint, SuccessMessages.LOBBIES_LOADED, lobbies);
    }


    // to create a lobby
    @PutMapping("/create-lobby")
     public ResponseEntity<?> createLobby(@RequestBody Map<String, String> params, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);


        Player playerCreator = (Player) session.getAttribute("player");
        int idCreator = playerCreator.getId();

        LoggerUtil.logInfo("param: "+ params);
        LoggerUtil.logInfo("param tipo: "+ params.get("tipo"));


        if ( !params.get("tipo").equals("Pubblica") && !params.get("tipo").equals("Privata") ) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.LOBBY_TYPE_NOT_SUPPORTED);
        }

        Lobby newLobby = new Lobby();
        newLobby.setCode(createLobbyCode());
        newLobby.setCreatorID(idCreator);
        newLobby.setCreationDate(LocalDateTime.now());
        newLobby.setMinNumPlayer(6);
        newLobby.setNumPlayer(1);
        newLobby.setMaxNumPlayer(18);
        newLobby.setType(params.get("tipo"));
        newLobby.setState("Attesa giocatori");

        lobbyDAO.save(newLobby);
        lobbyLists.addLobbyCode(newLobby.getCode());
        lobbyLists.addPlayer(playerCreator, newLobby.getCode());

        return messagesResponse.createResponse(endpoint, SuccessMessages.LOBBY_CREATED, newLobby);
    }

    // to delete a created lobby
    @DeleteMapping("/delete-lobby")
    public ResponseEntity<?> deleteLobby(@RequestBody Map<String, Integer> params, HttpServletRequest request){
        String endpoint = RequestService.getEndpoint(request);

        int code = params.get("code");
        lobbyDAO.deleteById(code);
        lobbyLists.removeLobbyCode(code);

        return messagesResponse.createResponse(endpoint, SuccessMessages.LOBBY_DELETED);
    }

    // for the player who wants to join a lobby
    @PostMapping("/join-lobby")
    public ResponseEntity<String> joinLobby(@RequestBody Map<String, Integer> params, HttpSession session, HttpServletRequest request) {
        String endpoint = RequestService.getEndpoint(request);

        int code = params.get("code");
        LoggerUtil.logInfo("Joining lobby " + code);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);
        }


        if (lobbyDAO.count() == 0) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.LOBBY_NOT_FOUND);
        }

        if (lobbyLists.getListSize() == 0) {
            List<Lobby> lobbies = lobbyDAO.findAll();
            for (Lobby lobby : lobbies) {
                lobbyLists.addLobbyCode(lobby.getCode());
            }
        }

        if (!lobbyLists.containsCode(code))
            return messagesResponse.createResponse(endpoint, ErrorMessages.LOBBY_NOT_FOUND);

        Lobby lobby = lobbyDAO.findLobbyByCode(code);

        if (lobbyLists.getListPlayers(code).size() == lobby.getMaxNumPlayer()) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.LIMIT_PLAYER_LOBBY);
        }

        if (lobby.getState().equals("In corso")) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.LOBBY_ALREADY_STARTED);
        }

        Player player = (Player) session.getAttribute("player");
        if (lobbyLists.containsPlayer(code, player))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_ALREADY_JOIN);

        checkIfExistsLobbyInvitation(lobby, player);

        lobbyLists.addPlayer(player, code);
        lobby.setNumPlayer(lobbyLists.getListPlayers(code).size());

        return messagesResponse.createResponse(endpoint, SuccessMessages.PLAYER_ADDED_LOBBY, lobbyLists.getListPlayers(code));
    }

    @PostMapping("/modify-lobby")
    public ResponseEntity<?> modifyLobby(@RequestBody ModifyLobbyRequest modifyLobbyRequest, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        int code = modifyLobbyRequest.getCodeLobby();
        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        if (!lobbyLists.containsCode(code))
            return messagesResponse.createResponse(endpoint, ErrorMessages.LOBBY_NOT_FOUND);

        Lobby lobby = lobbyDAO.findLobbyByCode(code);
        lobby.setMinNumPlayer(modifyLobbyRequest.getMinNumPlayer());
        lobby.setMaxNumPlayer(modifyLobbyRequest.getMaxNumPlayer());
        lobby.setNumPlayer(lobbyLists.getListPlayers(code).size());
        lobbyDAO.save(lobby);

        return messagesResponse.createResponse(endpoint, SuccessMessages.LOBBY_MODIFIED, lobby);
    }

    // for the player who wants to leave a lobby
    @PostMapping("/leave-lobby")
    public ResponseEntity<?> leaveLobby(@RequestBody Map<String, Integer> params, HttpSession session, HttpServletRequest request) {
        String endpoint = RequestService.getEndpoint(request);

        int code = params.get("codeLobby");

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        Player player = (Player) session.getAttribute("player");
        if (!lobbyLists.containsPlayer(code, player))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_FOUND);

        lobbyLists.removePlayer(player, code);
        Lobby lobby = lobbyDAO.findLobbyByCode(code);
        lobby.setNumPlayer(lobbyLists.getListPlayers(code).size());

        if (lobby.getNumPlayer() <= 0){
            lobbyDAO.delete(lobby);
            lobbyLists.removeLobbyCode(code);
        }

        return messagesResponse.createResponse(endpoint, SuccessMessages.PLAYER_REMOVED_LOBBY, player);
    }

    @PostMapping("/invite-friend-lobby")
    public ResponseEntity<?> inviteFriendLobby(@RequestBody InviteFriendToLobbyRequest inviteFriendToLobbyRequest, HttpSession session, HttpServletRequest request) {
        String endpoint = RequestService.getEndpoint(request);

        if(!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        List<Object> out = new ArrayList<>();

        Player player = (Player) session.getAttribute("player");

        LobbyInvitation lobbyInvitation = new LobbyInvitation();
        lobbyInvitation.setSendingPlayerId(player.getId());
        lobbyInvitation.setInvitedPlayerId(inviteFriendToLobbyRequest.getFriendId());
        lobbyInvitation.setDataInvitation(LocalDateTime.now());
        lobbyInvitationDAO.save(lobbyInvitation);

        ResponseEntity<?> responseNotify = notificationCaller.sendNotificationWebClient(String.valueOf(inviteFriendToLobbyRequest.getFriendId()), "Invito ad entrare in lobby", player);

        out.add(responseNotify);
        out.add(lobbyInvitation);

        return messagesResponse.createResponse(endpoint, SuccessMessages.NOTIFICATION_SENT, out);
    }


    public int createLobbyCode(){
        Random random = new Random();
        int lobbyCode;
        boolean isNotUnique;

        do {
            //generates a number beetween 100000 and 999999...
            lobbyCode = 100000 + random.nextInt(900000);
            isNotUnique = lobbyDAO.existsLobbyByCode(lobbyCode);
        } while (isNotUnique); // ...unique

        return lobbyCode;
    }

    public void checkIfExistsLobbyInvitation(Lobby lobbyToJoin, Player playerInSession){
        LobbyInvitationPk lobbyInvitationPk = new LobbyInvitationPk();
        lobbyInvitationPk.setSendingPlayerId(lobbyToJoin.getCreatorID());
        lobbyInvitationPk.setInvitedPlayerId(playerInSession.getId());

        LobbyInvitation invitation =
                lobbyInvitationDAO.findLobbyInvitationByLobbyInvitationPk(lobbyInvitationPk);

        if (invitation != null){
            lobbyInvitationDAO.delete(invitation);
        }
    }

    public static LobbyDAO getLobbyDAO() {
        return lobbyDAO;
    }

}
