package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


public interface LobbyDAO extends JpaRepository<Lobby, Integer> {

    boolean existsLobbyByCode(@RequestParam int code);

    Lobby findLobbyByCode(@RequestParam int code);

    List<Lobby> findAllByType(@RequestParam String type);
}
