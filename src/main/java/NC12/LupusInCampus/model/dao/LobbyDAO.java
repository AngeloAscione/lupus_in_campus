package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


public interface LobbyDAO extends JpaRepository<Lobby, Integer> {

    boolean existsLobbyByCode(@RequestParam int code);

    Lobby findLobbyByCode(@RequestParam int code);

    List<Lobby> findAllByType(@RequestParam String type);

}
