package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.Giocatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiocatoreDAO extends JpaRepository<Giocatore, Long> {
    Giocatore findGiocatoreByEmail(String email);

    Giocatore findGiocatoreByNickname(String nickname);

    Giocatore findGIocatoreById(int id);

    boolean existsGiocatoreByEmailAndPassword(String email, String password);
}