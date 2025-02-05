package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.Player;
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

import java.time.LocalDateTime;
import java.util.List;
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

    // TODO aggiungere la funzione per prendere le richieste di invito lobby del giocatore

    //to receive a list of all active public lobbies
    @GetMapping("/active-public-lobbies")
    public ResponseEntity<?> getActivePublicLobbies(HttpSession session) {

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

        return ResponseEntity.ok().body(response);
    }


    // to create a lobby
    @GetMapping("/create-lobby")
    public ResponseEntity<?> createLobby(@RequestParam String minNumPlayer,
                                         @RequestParam String maxNumPlayer,
                                             @RequestParam String tipo /*'Pubblica' o 'Privata'*/,
                                         HttpSession session) {

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
        newLobby.setMinNumPlayer(Integer.parseInt(minNumPlayer));
        newLobby.setNumPlayer(1); //here should always be 1
        newLobby.setMaxNumPlayer(Integer.parseInt(maxNumPlayer));
        newLobby.setType(tipo);
        newLobby.setState("Attesa giocatori");

        lobbyDAO.save(newLobby);
        lobbyLists.addLobbyCode(newLobby.getCode());
        lobbyLists.addPlayer(playerCreator, newLobby.getCode());

        MessageResponse response = new MessageResponse(
                SuccessMessages.LOBBY_CREATED.getCode(),
                SuccessMessages.LOBBY_CREATED.getMessage(),
                newLobby
        );
        return ResponseEntity.ok().body(response);


    }

    // to delete a created lobby
    @GetMapping("/delete-lobby")
    public ResponseEntity<?> deleteLobby(@RequestParam String code){
        lobbyDAO.deleteById(Integer.parseInt(code));
        lobbyLists.removeLobbyCode(Integer.parseInt(code));
        return ResponseEntity.ok().body(new MessageResponse(
                SuccessMessages.LOBBY_DELETED.getCode(),
                SuccessMessages.LOBBY_DELETED.getMessage())
        );
    }

    // for the player who wants to join a lobby
    @GetMapping("/join-lobby")
    public ResponseEntity<?> joinLobby(@RequestParam String codeLobby, HttpSession session) {
        MessageResponse response;
        int code = Integer.parseInt(codeLobby);

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
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/modify-lobby")
    public ResponseEntity<?> modifyLobby(@RequestParam String codeLobby,
                                         @RequestParam String minNumPlayer,
                                         @RequestParam String maxNumPlayer,
                                         HttpSession session) {

        int code = Integer.parseInt(codeLobby);

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
        lobby.setMinNumPlayer(Integer.parseInt(minNumPlayer));
        lobby.setMaxNumPlayer(Integer.parseInt(maxNumPlayer));
        lobby.setNumPlayer(lobbyLists.getListPlayers(code).size());
        lobbyDAO.save(lobby);

        MessageResponse response = new MessageResponse(
            SuccessMessages.LOBBY_MODIFIED.getCode(),
            SuccessMessages.LOBBY_MODIFIED.getMessage(),
            lobby
        );

        return ResponseEntity.ok().body(response);

    }

    // for the player who wants to leave a lobby
    @GetMapping("/leave-lobby")
    public ResponseEntity<?> leaveLobby(@RequestParam String codeLobby, HttpSession session) {
        int code = Integer.parseInt(codeLobby);

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
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/invite-friend-lobby")
    public ResponseEntity<?> inviteFriendLobby(@RequestParam String idFriend,
                                                     @RequestParam String codeLobby,
                                                     HttpSession session) {
        if (!Session.sessionIsActive(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new MessageResponse(
                            ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                            ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
                    )
            );
        }

        Player player = (Player) session.getAttribute("player");

        LobbyInvitation lobbyInvitation = new LobbyInvitation();
        lobbyInvitation.setSendingPlayerId(player.getId());
        lobbyInvitation.setInvitedPlayerId(Integer.parseInt(idFriend));
        lobbyInvitation.setDataInvitation(LocalDateTime.now());
        lobbyInvitationDAO.save(lobbyInvitation);

        return notificationCaller.sendNotificationWebClient(idFriend, "Invito ad entrare in lobby");

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
