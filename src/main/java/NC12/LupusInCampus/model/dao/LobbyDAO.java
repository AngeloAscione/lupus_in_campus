package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


public interface LobbyDAO extends JpaRepository<Lobby, Integer> {

    boolean existsLobbyByCode(@RequestParam int code);

    Lobby findLobbyByCode(@RequestParam int code);

    List<Lobby> findAllByType(@RequestParam String type);

    @Transactional
    @Modifying
    @Query(value = "UPDATE lobby " +
            "SET numGiocatoriMin = :numMin, " +
            "numGiocatoriMax = :numMax, " +
            "numGiocatori = :num " +
            "WHERE codice = :code",nativeQuery = true)
    void updateLobbyByCode(@Param("code") int code, @Param("numMin") int numGiocatoriMin,
                            @Param("numMax") int numGiocatoriMax, @Param("num") int numGiocatori);
}
