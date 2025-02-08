package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dao.FriendDAO;
import NC12.LupusInCampus.model.dao.GameDAO;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.model.Game;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.service.RequestService;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final MessagesResponse messagesResponse;

    @Autowired
    public PlayerAreaController(GameDAO gameDAO, FriendDAO friendDAO, PlayerDAO playerDAO, MessagesResponse messagesResponse) {
        this.gameDAO = gameDAO;
        this.friendDAO = friendDAO;
        this.playerDAO = playerDAO;
        this.messagesResponse = messagesResponse;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getPlayerArea(HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session)){
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);
        }

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
        return messagesResponse.createResponse(endpoint, SuccessMessages.LOAD_ALL_INFO, allInfo);
    }

    @PostMapping("/edit-player-data")
    public ResponseEntity<?> editPlayerData(HttpSession session, @RequestBody Map<String, String> param, HttpServletRequest request) {
        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        String newNickname = param.get("newNickname");

        if (playerDAO.findPlayerByNickname(newNickname) != null){
            return messagesResponse.createResponse(endpoint, ErrorMessages.NICKNAME_ALREADY_USED);
        }

        Player player = (Player) session.getAttribute("player");
        player.setNickname(newNickname);
        playerDAO.save(player);

        player.setNickname(newNickname);
        session.setAttribute("player", player);

        return messagesResponse.createResponse(endpoint, SuccessMessages.SUCCESS_EDIT, player);
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
