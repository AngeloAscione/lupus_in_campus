package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.Giocatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiocatoreDAO extends JpaRepository<Giocatore, Long> {
    // Non serve implementarle, fa tutto Spring usando i nomi e i tipi delle varibili giusti

    Giocatore findGiocatoreByEmail(String email);

    Giocatore findGiocatoreByNickname(String nickname);

    Giocatore findGIocatoreById(int id);

    boolean existsGiocatoreByEmailAndPassword(String email, String password);

}