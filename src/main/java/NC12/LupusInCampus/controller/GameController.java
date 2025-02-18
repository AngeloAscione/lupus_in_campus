package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.Game;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.GameDAO;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.dto.game.GameUpdateMessage;
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("controller/game")
public class GameController {

    private final LobbyDAO lobbyDAO;
    private final ListPlayersLobbiesService lobbyLists = new ListPlayersLobbiesService();
    private static GameDAO gameDAO = null;
    private final MessagesResponse messagesResponse;
    private final PlayerDAO playerDAO;

    @Autowired
    public GameController(LobbyDAO lobbyDAO, GameDAO gameDAO_param, MessagesResponse messagesResponse, PlayerDAO playerDAO) {
        this.lobbyDAO = lobbyDAO;
        gameDAO = gameDAO_param;
        this.messagesResponse = messagesResponse;
        this.playerDAO = playerDAO;
    }

    @PostMapping("/start-game")
    public ResponseEntity<?> saveLobbyGameState(@RequestBody Map<String, String> params, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);
        String codeLobby = params.get("codeLobby");

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

        //NOTIFY ALL PLAYERS GAME IS STARTING
        //WebSocketGameController.notifyGameUpdate(new GameUpdateMessage("GAME_STARTED", "", String.valueOf(lobby.getCode())));

        Player me = playerDAO.findPlayerById(Integer.parseInt(params.get("playerID")));
        List<Player> listPlayerWithRole = roleAssignment(listPlayer);
        for (int i = 0; i < listPlayerWithRole.size() ; i++) {
            Player player = listPlayerWithRole.get(i);
            gameDAO.saveParticipants(game.getId(), player.getId(), player.getRole());
            if (player.getId() == Integer.parseInt(params.get("playerID"))) {
                me = player;
            } else {
                WebSocketGameController.addToQueue(player.getNickname(), new GameUpdateMessage("ROLE", player.getRole(), String.valueOf(lobby.getCode())));
            }
        }

        return messagesResponse.createResponse(endpoint, SuccessMessages.LOBBY_GAME_STATUS_SAVED, me);
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


    public static GameDAO getGameDAO() {
        return gameDAO;
    }
}
