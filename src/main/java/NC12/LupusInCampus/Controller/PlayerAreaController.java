package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.FriendDAO;
import NC12.LupusInCampus.Model.DAO.GameDAO;
import NC12.LupusInCampus.Model.Enums.ErrorMessages;
import NC12.LupusInCampus.Model.Enums.SuccessMessages;
import NC12.LupusInCampus.Model.Game;
import NC12.LupusInCampus.Model.Player;
import NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer.MessageResponse;
import NC12.LupusInCampus.Model.Utils.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("controller/player-area")
public class PlayerAreaController {

    private final GameDAO gameDAO;
    private final FriendDAO friendDAO;

    @Autowired
    public PlayerAreaController(GameDAO gameDAO, FriendDAO friendDAO) {
        this.gameDAO = gameDAO;
        this.friendDAO = friendDAO;
    }

    @GetMapping("")
    public ResponseEntity<?> getPlayerArea(HttpSession session) {
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
        return ResponseEntity.ok().body(new MessageResponse(
                SuccessMessages.LOAD_ALL_INFO.getCode(),
                SuccessMessages.LOAD_ALL_INFO.getMessage(),
                allInfo
        ));
    }

    public List<Game> getInfoGamesPartecipated(Player player) {
        List<Game> listGamesParticipated = gameDAO.findGamesPartecipatedByIdPlayer(player.getId());

        // with information
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
