package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerDAO extends JpaRepository<Player, Long> {
    //No implementation needed, Spring does it all using the right method names and variable types

    Player findPlayerByEmail(String email);

    Player findPlayerByNickname(String nickname);

    Player findPlayerById(int id);

    @Query(value = "SELECT p.* FROM giocatore p " +
            "JOIN listaamici l " +
            "ON l.giocatoreProprietario = \"9\" " +
            "WHERE p.ID = l.giocatoreAmico"
            , nativeQuery = true)
    List<Player> findFriendsByPlayerId(int id);
}