package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.dto.lobby.CreateLobbyRequest;
import NC12.LupusInCampus.dto.lobby.InviteFriendToLobbyRequest;
import NC12.LupusInCampus.dto.lobby.ModifyLobbyRequest;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.service.ListPlayersLobbiesService;
import NC12.LupusInCampus.utils.clientServerComunication.MessageResponse;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.model.dao.LobbyInvitationDAO;
import NC12.LupusInCampus.model.LobbyInvitation;
import NC12.LupusInCampus.utils.clientServerComunication.NotificationCaller;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.LoggingUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("controller/lobby")
public class LobbyController {

    private final LobbyDAO lobbyDAO;
    private final LobbyInvitationDAO lobbyInvitationDAO;
    private final NotificationCaller notificationCaller;
    private final ListPlayersLobbiesService lobbyLists = new ListPlayersLobbiesService();


    @Autowired
    public LobbyController(LobbyDAO lobbyDAO, LobbyInvitationDAO lobbyInvitationDAO, NotificationCaller notificationCaller) {
        this.lobbyDAO = lobbyDAO;
        this.lobbyInvitationDAO = lobbyInvitationDAO;
        this.notificationCaller = notificationCaller;
    }

    @GetMapping("/lobby-invitations")
    public ResponseEntity<?> getLobbyInvitations(HttpSession session) {
        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");

        List<LobbyInvitation> invitations =  lobbyInvitationDAO.findLobbyInvitationsByLobbyInvitationPkInvitedPlayerId(player.getId());

        return ResponseEntity.ok().body(invitations);
    }

    //to receive a list of all active public lobbies
    @GetMapping("/active-public-lobbies")
    public ResponseEntity<?> getActivePublicLobbies(HttpSession session) {

        LoggerUtil.logInfo("-> Ricevuta richiesta get active public lobbies");
        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        if (lobbyDAO.count() == 0) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PUBLIC_LOBBIES_NOT_FOUND.getCode(),
                    ErrorMessages.PUBLIC_LOBBIES_NOT_FOUND.getMessage()
            )
        );


        List<Lobby> lobbies = lobbyDAO.findAllByType("Pubblica"); //public
        MessageResponse response = new MessageResponse(
                SuccessMessages.LOBBIES_LOADED.getCode(),
                SuccessMessages.LOBBIES_LOADED.getMessage(),
                lobbies
        );

        LoggerUtil.logInfo("<- Risposta get active public lobbies");
        return ResponseEntity.ok().body(response);
    }


    // to create a lobby
    @PutMapping("/create-lobby")
    public ResponseEntity<?> createLobby(@RequestBody CreateLobbyRequest createLobbyRequest, HttpSession session) {

        LoggerUtil.logInfo("-> Ricevuta richiesta create lobby");
        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );


        Player playerCreator = (Player) session.getAttribute("player");
        int idCreator = playerCreator.getId();

        Lobby newLobby = new Lobby();
        newLobby.setCode(createLobbyCode());
        newLobby.setCreatorID(idCreator);
        newLobby.setCreationDate(LocalDateTime.now());
        newLobby.setMinNumPlayer(createLobbyRequest.getMinNumPlayer());
        newLobby.setNumPlayer(1); //here should always be 1
        newLobby.setMaxNumPlayer(createLobbyRequest.getMaxNumPlayer());
        newLobby.setType(createLobbyRequest.getTipo());
        newLobby.setState("Attesa giocatori");

        lobbyDAO.save(newLobby);
        lobbyLists.addLobbyCode(newLobby.getCode());
        lobbyLists.addPlayer(playerCreator, newLobby.getCode());

        MessageResponse response = new MessageResponse(
                SuccessMessages.LOBBY_CREATED.getCode(),
                SuccessMessages.LOBBY_CREATED.getMessage(),
                newLobby
        );

        LoggerUtil.logInfo("<- Risposta create lobby");
        return ResponseEntity.ok().body(response);
    }

    // to delete a created lobby
    @DeleteMapping("/delete-lobby")
    public ResponseEntity<?> deleteLobby(@RequestBody Map<String, Integer> params){
        LoggerUtil.logInfo("-> Ricevuta richiesta delete lobby");
        int code = params.get("code");
        lobbyDAO.deleteById(code);
        lobbyLists.removeLobbyCode(code);
        LoggerUtil.logInfo("<- Risposta delete lobby");
        return ResponseEntity.ok().body(new MessageResponse(
                SuccessMessages.LOBBY_DELETED.getCode(),
                SuccessMessages.LOBBY_DELETED.getMessage())
        );
    }

    // for the player who wants to join a lobby
    @PostMapping("/join-lobby")
    public ResponseEntity<?> joinLobby(@RequestBody Map<String, Integer> params, HttpSession session) {
        LoggerUtil.logInfo("-> Ricevuta richiesta join lobby");
        MessageResponse response;
        int code = params.get("code");

        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        if (!lobbyLists.containsCode(code)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.LOBBY_NOT_FOUND.getCode(),
                    ErrorMessages.LOBBY_NOT_FOUND.getMessage()
            )
        );

        Lobby lobby = lobbyDAO.findLobbyByCode(code);
        if (lobbyLists.getListPlayers(code).size() >= lobby.getMaxNumPlayer()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.LIMIT_PLAYER_LOBBY.getCode(),
                    ErrorMessages.LIMIT_PLAYER_LOBBY.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");
        if (lobbyLists.getListPlayers(code).contains(player)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_ALREADY_JOIN.getCode(),
                    ErrorMessages.PLAYER_ALREADY_JOIN.getMessage()
            )
        );


        lobbyLists.addPlayer(player, code);
        lobby.setNumPlayer(lobbyLists.getListPlayers(code).size());

        response = new MessageResponse(
            SuccessMessages.PLAYER_ADDED_LOBBY.getCode(),
            SuccessMessages.PLAYER_ADDED_LOBBY.getMessage(),
            lobby
        );
        LoggerUtil.logInfo("<- Risposta join lobby");
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/modify-lobby")
    public ResponseEntity<?> modifyLobby(@RequestBody ModifyLobbyRequest modifyLobbyRequest, HttpSession session) {

        LoggerUtil.logInfo("-> Ricevuta richiesta modify lobby");

        int code = modifyLobbyRequest.getCodeLobby();
        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponse(
                        ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                        ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
                )
        );

        if (!lobbyLists.containsCode(code)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponse(
                        ErrorMessages.LOBBY_NOT_FOUND.getCode(),
                        ErrorMessages.LOBBY_NOT_FOUND.getMessage()
                )
        );


        Lobby lobby = lobbyDAO.findLobbyByCode(code);
        lobby.setMinNumPlayer(modifyLobbyRequest.getMinNumPlayer());
        lobby.setMaxNumPlayer(modifyLobbyRequest.getMaxNumPlayer());
        lobby.setNumPlayer(lobbyLists.getListPlayers(code).size());
        lobbyDAO.save(lobby);

        MessageResponse response = new MessageResponse(
            SuccessMessages.LOBBY_MODIFIED.getCode(),
            SuccessMessages.LOBBY_MODIFIED.getMessage(),
            lobby
        );

        LoggerUtil.logInfo("<- Ricevuta modify lobby");
        return ResponseEntity.ok().body(response);

    }

    // for the player who wants to leave a lobby
    @PostMapping("/leave-lobby")
    public ResponseEntity<?> leaveLobby(@RequestParam Map<String, Integer> params, HttpSession session) {
        LoggerUtil.logInfo("-> Ricevuta richiesta leave lobby");
        int code = params.get("codeLobby");

        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");
        if (!lobbyLists.containsPlayer(code, player)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                ErrorMessages.PLAYER_NOT_FOUND.getCode(),
                ErrorMessages.PLAYER_NOT_FOUND.getMessage()
            )
        );

        lobbyLists.removePlayer(player, code);
        Lobby lobby = lobbyDAO.findLobbyByCode(code);
        lobby.setNumPlayer(lobbyLists.getListPlayers(code).size());

        MessageResponse response = new MessageResponse(
                SuccessMessages.PLAYER_REMOVED_LOBBY.getCode(),
                SuccessMessages.PLAYER_REMOVED_LOBBY.getMessage(),
                lobby
        );
        LoggerUtil.logInfo("<- Risposta leave lobby");
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/invite-friend-lobby")
    public ResponseEntity<?> inviteFriendLobby(@RequestBody InviteFriendToLobbyRequest inviteFriendToLobbyRequest, HttpSession session) {

        LoggerUtil.logInfo("-> Ricevuta richiesta invite friend to lobby");
        if(!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponse(
                        ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                        ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
                )
        );

        List<Object> out = new ArrayList<>();

        Player player = (Player) session.getAttribute("player");

        LobbyInvitation lobbyInvitation = new LobbyInvitation();
        lobbyInvitation.setSendingPlayerId(player.getId());
        lobbyInvitation.setInvitedPlayerId(inviteFriendToLobbyRequest.getFriendId());
        lobbyInvitation.setDataInvitation(LocalDateTime.now());
        lobbyInvitationDAO.save(lobbyInvitation);

        ResponseEntity responseNotify = notificationCaller.sendNotificationWebClient(String.valueOf(inviteFriendToLobbyRequest.getFriendId()), "Invito ad entrare in lobby");

        out.add(responseNotify);
        out.add(lobbyInvitation);

        LoggerUtil.logInfo("<- Risposta invite friend to lobby");
        return ResponseEntity.ok().body(out);
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

}
