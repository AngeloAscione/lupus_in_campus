package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.FriendDAO;
import NC12.LupusInCampus.Model.DAO.PlayerDAO;
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

import java.util.List;

@RestController
@RequestMapping("controller/friend")
public class FriendController {

    private final PlayerDAO playerDAO;
    private final FriendDAO friendDAO;

    @Autowired
    public FriendController(PlayerDAO playerDAO, FriendDAO friendDAO) {
        this.playerDAO = playerDAO;
        this.friendDAO = friendDAO;
    }

    @GetMapping("/remove-friend")
    public ResponseEntity<?> removeFriend(@RequestParam String id, HttpSession session) {

        Player player = (Player) session.getAttribute("player");
        if (player != null) {
            player.setFriendsList(friendDAO.findFriendsByPlayerId(player.getId()));

            List<Player> friends = player.getFriendsList();

            System.out.println(friends);

            int idToRemove = Integer.parseInt(id);
            Player friendToRemove = playerDAO.findPlayerById(idToRemove);

            if (friends.contains(friendToRemove)) {
                friendDAO.removeFriendById(player.getId(), friendToRemove.getId());

                MessageResponse response = new MessageResponse(
                        HttpStatus.OK.value(),
                        HttpStatus.OK.getReasonPhrase(),
                        "Eliminazione effettuta"
                );
                return ResponseEntity.ok().body(response);

            }else {
                MessageResponse response = new MessageResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "Eliminazione non riuscita"
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        }

        MessageResponse response = new MessageResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Sessione non presente"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);


    }


}
