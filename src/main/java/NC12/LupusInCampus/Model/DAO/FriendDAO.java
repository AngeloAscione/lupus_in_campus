package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FriendDAO extends JpaRepository<Player, Integer> {

    @Query(value = "SELECT p.* FROM giocatore p " +
            "JOIN listaamici l " +
            "ON l.giocatoreProprietario = :playerID " +
            "WHERE p.ID = l.giocatoreAmico"
            , nativeQuery = true)
    List<Player> findFriendsByPlayerId(@Param("playerID") int idPlayer);

    @Modifying
    @Transactional
    @Query (value = "DELETE FROM listaamici WHERE giocatoreProprietario = :ownerID AND giocatoreAmico = :friendID", nativeQuery = true)
    void removeFriendById(@Param("ownerID") int ownerID, @Param("friendID") int friendID);

    @Modifying
    @Transactional
    @Query (value = "INSERT INTO listaamici VALUES (:ownerID, :friendID);", nativeQuery = true)
    void addFriend(@Param("ownerID") int ownerID, @Param("friendID") int friendID);

    @Query(value = "SELECT g.* FROM giocatore g " +
            "JOIN richiestaamicizia r ON r.giocatoreMittente = g.ID " +
            "WHERE r.giocatoreDestinatario = :idPlayer",nativeQuery = true)
    List<Player> findPendingFriendRequests(@Param("idPlayer") int idPlayer);
}
