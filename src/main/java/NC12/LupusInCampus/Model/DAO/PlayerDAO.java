package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerDAO extends JpaRepository<Player, Long> {
    // Non serve implementarle, fa tutto Spring usando i nomi e i tipi delle varibili giusti

    Player findPlayerByEmail(String email);

    Player findPlayerByNickname(String nickname);

    Player findPlayerById(int id);

    boolean existsPlayerByEmailAndPassword(String email, String password);

}