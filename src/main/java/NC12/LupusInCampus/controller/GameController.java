package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.Game;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.GameDAO;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.PlayerRole;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.roleFactory.RoleAssignmentFactory;
import NC12.LupusInCampus.roleFactory.RoleAssignmentFactoryProvider;
import NC12.LupusInCampus.service.ListPlayersLobbiesService;
import NC12.LupusInCampus.service.RequestService;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("controller/game")
public class GameController {

    private final LobbyDAO lobbyDAO;
    private final ListPlayersLobbiesService lobbyLists = new ListPlayersLobbiesService();
    private final GameDAO gameDAO;
    private final MessagesResponse messagesResponse;

    @Autowired
    public GameController(LobbyDAO lobbyDAO, GameDAO gameDAO, MessagesResponse messagesResponse) {
        this.lobbyDAO = lobbyDAO;
        this.gameDAO = gameDAO;
        this.messagesResponse = messagesResponse;
    }

    @GetMapping("/save-lobby-game-state")
    public ResponseEntity<?> saveLobbyGameState(@RequestParam String codeLobby, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        if (!lobbyDAO.existsLobbyByCode(Integer.parseInt(codeLobby)))
            return messagesResponse.createResponse(endpoint, ErrorMessages.LOBBY_NOT_FOUND);

        // get lobby
        Lobby lobby = lobbyDAO.findLobbyByCode(Integer.parseInt(codeLobby));

        // list of lobby
        List<Player> listPlayer = lobbyLists.getListPlayers(lobby.getCode());

        // updated info
        lobby.setState("In corso");
        lobby.setNumPlayer(listPlayer.size());
        lobbyDAO.save(lobby);

        // save game
        Game game = new Game();
        game.setCreatorId(lobby.getCreatorID());
        game.setGameDate(LocalDateTime.now());
        gameDAO.save(game);

        // save participants
        List<Player> listPlayerWithRole = roleAssignment(listPlayer);
        for (Player player : listPlayerWithRole) {
            gameDAO.saveParticipants(game.getId(), player.getId(), player.getRole());
        }

        return messagesResponse.createResponse(endpoint, SuccessMessages.LOBBY_GAME_STATUS_SAVED);
    }

    public List<Player> roleAssignment(List<Player> players) {

        int numPlayers = players.size();
        LoggerUtil.logInfo(" -> Numero giocatori "+numPlayers);

        //abstract factory
        RoleAssignmentFactory factory = RoleAssignmentFactoryProvider.getFactory(numPlayers);
        List<PlayerRole> availableRoles = factory.getRoles(numPlayers);
        LoggerUtil.logInfo(" -> Ruoli possibili: "+availableRoles);

        // Randomly assign roles to players
        Collections.shuffle(players);
        for (int i = 0; i < availableRoles.size(); i++) {
            players.get(i).setRole(availableRoles.get(i).getText());
        }

        // The rest are farmers (student in course)
        for (int i = availableRoles.size(); i < numPlayers; i++) {
            players.get(i).setRole(PlayerRole.STUDENT_IN_COURSE.getText());
        }

        LoggerUtil.logInfo(" -> Lista player con ruoli: "+players);
        return players;
    }

}
