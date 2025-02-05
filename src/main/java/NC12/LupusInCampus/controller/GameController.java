package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.Game;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.GameDAO;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.PlayerRole;
import NC12.LupusInCampus.service.ListPlayersLobbiesService;
import NC12.LupusInCampus.utils.clientServerComunication.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("controller/game")
public class GameController {

    private final LobbyDAO lobbyDAO;
    private final ListPlayersLobbiesService lobbyLists = new ListPlayersLobbiesService();
    private final GameDAO gameDAO;

    @Autowired
    public GameController(LobbyDAO lobbyDAO, GameDAO gameDAO) {
        this.lobbyDAO = lobbyDAO;
        this.gameDAO = gameDAO;
    }

    @GetMapping("/play-save")
    public ResponseEntity<?> playSave(@RequestParam String codeLobby) {
        if (!lobbyDAO.existsLobbyByCode(Integer.parseInt(codeLobby))) {
            new MessageResponse(
                ErrorMessages.LOBBY_NOT_FOUND.getCode(),
                ErrorMessages.LOBBY_NOT_FOUND.getMessage()
            );
        }

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

        return ResponseEntity.ok().body("ok, gestione salvata inizo partita");
    }

    public List<Player> roleAssignment(List<Player> players) {

        PlayerRole[] roles = PlayerRole.values();

        for (Player player : players) {
            PlayerRole randomRole = roles[ThreadLocalRandom.current().nextInt(roles.length)];
            player.setRole(randomRole.getText());
        }

        return players;
    }


}
