package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Repository
public interface PlayerDAO extends JpaRepository<Player, Integer> {
    //No implementation needed, Spring does it all using the right method names and variable types

    Player findPlayerByEmail(@RequestParam String email);

    Player findPlayerByNickname(@RequestParam String nickname);

    Player findPlayerById(@RequestParam int id);

    List<Player> findPlayersByNicknameContainingIgnoreCase(@RequestParam String query);
}