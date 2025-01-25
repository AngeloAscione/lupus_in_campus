package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.LobbyDAO;
import NC12.LupusInCampus.Model.Enums.ErrorMessages;
import NC12.LupusInCampus.Model.Enums.SuccessMessages;
import NC12.LupusInCampus.Model.Lobby;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("controller/lobby")
public class LobbyController {

    private final LobbyDAO lobbyDAO;
    private static final Map<Integer, List<Player>> lobbyLists = new ConcurrentHashMap<>();

    @Autowired
    public LobbyController(LobbyDAO lobbyDAO) {this.lobbyDAO = lobbyDAO;}

    //to receive a list of all active public lobbies
    @GetMapping("/active-public-lobbies")
    public ResponseEntity<?> getActivePublicLobbies(HttpSession session) {

        if (!sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
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

        if (!sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
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
        lobbyLists.putIfAbsent(newLobby.getCode(), new ArrayList<>());
        lobbyLists.get(newLobby.getCode()).add(playerCreator);

        MessageResponse response = new MessageResponse(
                SuccessMessages.LOBBY_CREATED.getCode(),
                SuccessMessages.LOBBY_CREATED.getMessage(),
                newLobby
        );
        System.out.println("Sezione creazione lobby: "+lobbyLists);
        return ResponseEntity.ok().body(response);


    }

    // to delete a created lobby
    @GetMapping("/delete-lobby")
    public ResponseEntity<?> deleteLobby(@RequestParam String code){
        lobbyDAO.deleteById(Integer.parseInt(code));
        lobbyLists.remove(Integer.parseInt(code));
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

        if (!sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        if (!lobbyLists.containsKey(code)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.LOBBY_NOT_FOUND.getCode(),
                    ErrorMessages.LOBBY_NOT_FOUND.getMessage()
            )
        );

        Lobby lobby = lobbyDAO.findLobbyByCode(code);
        if (lobbyLists.get(code).size() >= lobby.getMaxNumPlayer()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.LIMIT_PLAYER_LOBBY.getCode(),
                    ErrorMessages.LIMIT_PLAYER_LOBBY.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");
        if (lobbyLists.get(code).contains(player)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponse(
                        ErrorMessages.PLAYER_ALREADY_JOIN.getCode(),
                        ErrorMessages.PLAYER_ALREADY_JOIN.getMessage()
                )
        );


        lobbyLists.get(code).add(player);
        lobby.setNumPlayer(lobbyLists.get(code).size());


        System.out.println("Sezione join lobby: "+lobbyLists);

        response = new MessageResponse(
                SuccessMessages.PLAYER_ADDED_LOBBY.getCode(),
                SuccessMessages.PLAYER_ADDED_LOBBY.getMessage(),
                lobby
        );
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/leave-lobby")
    public ResponseEntity<?> leaveLobby(@RequestParam String codeLobby, HttpSession session) {
        MessageResponse response;
        int code = Integer.parseInt(codeLobby);

        if (!sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");
        if (!lobbyLists.get(code).contains(player)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponse(
                        ErrorMessages.PLAYER_NOT_FOUND.getCode(),
                        ErrorMessages.PLAYER_NOT_FOUND.getMessage()
                )
        );

        lobbyLists.get(code).remove(player);

        response = new MessageResponse(
                SuccessMessages.PLAYER_REMOVED_LOBBY.getCode(),
                SuccessMessages.PLAYER_REMOVED_LOBBY.getMessage()
        );
        return ResponseEntity.ok().body(response);
    }
/*
    @GetMapping("/invite-friend-lobby")
    public ResponseEntity<?> inviteFriendLobby(@RequestParam String idFriend, @RequestParam String codeLobby, HttpSession session) {

    }

    */

    public boolean sessionIsActive(HttpSession session) {
        return session.getAttribute("player") != null;
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
