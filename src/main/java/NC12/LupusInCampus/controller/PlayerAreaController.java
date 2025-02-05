package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dao.FriendDAO;
import NC12.LupusInCampus.model.dao.GameDAO;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.model.Game;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.clientServerComunication.MessageResponse;
import NC12.LupusInCampus.utils.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("controller/player-area")
public class PlayerAreaController {

    private final GameDAO gameDAO;
    private final FriendDAO friendDAO;
    private final PlayerDAO playerDAO;

    @Autowired
    public PlayerAreaController(GameDAO gameDAO, FriendDAO friendDAO, PlayerDAO playerDAO) {
        this.gameDAO = gameDAO;
        this.friendDAO = friendDAO;
        this.playerDAO = playerDAO;
    }

    @GetMapping("")
    public ResponseEntity<?> getPlayerArea(HttpSession session) {
        LoggerUtil.logInfo("-> Ricevuta richiesta get Player Area");
        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        List<Object> allInfo = new ArrayList<>();

        // info player
        Player player = (Player) session.getAttribute("player");
        allInfo.add(player);

        // all the games he has participated in
        List<Game> listGamesParticipated = getInfoGamesPartecipated(player);
        allInfo.add(listGamesParticipated);

        // pending friend requests he has
        allInfo.add(friendDAO.findPendingFriendRequests(player.getId()));

        // return all info
        LoggerUtil.logInfo("<- Risposta get Player Area");
        return ResponseEntity.ok().body(new MessageResponse(
                SuccessMessages.LOAD_ALL_INFO.getCode(),
                SuccessMessages.LOAD_ALL_INFO.getMessage(),
                allInfo
        ));
    }

    @PostMapping("/edit-player-data")
    public ResponseEntity<?> editPlayerData(HttpSession session, @RequestBody Map<String, String> param) {
        LoggerUtil.logInfo("-> Ricevuta richiesta edit Player data");
        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                    ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
            )
        );

        String newNickname = param.get("newNickname");

        if (playerDAO.findPlayerByNickname(newNickname) != null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new MessageResponse(
                    ErrorMessages.NICKNAME_ALREADY_USED.getCode(),
                    ErrorMessages.NICKNAME_ALREADY_USED.getMessage()
            )
        );

        Player player = (Player) session.getAttribute("player");
        player.setNickname(newNickname);
        playerDAO.save(player);

        player.setNickname(newNickname);
        session.setAttribute("player", player);

        LoggerUtil.logInfo("<- Rispsota richiesta edit Player data");
        return ResponseEntity.ok().body(
            new MessageResponse(
                    SuccessMessages.SUCCESS_EDIT.getCode(),
                    SuccessMessages.SUCCESS_EDIT.getMessage(),
                    player
            )
        );
    }


    public List<Game> getInfoGamesPartecipated(Player player) {
        List<Game> listGamesParticipated = gameDAO.findGamesPartecipatedByIdPlayer(player.getId());

        for (Game game : listGamesParticipated) {
            List<Player> partecipants = gameDAO.findPartecipantsByIdGame(game.getId());
            for (Player partecipant : partecipants) {
                partecipant.setRole(gameDAO.findRoleByPlayerId(partecipant.getId(), game.getId()));
            }
            game.setParticipants(partecipants);
        }

        return listGamesParticipated;
    }
}
