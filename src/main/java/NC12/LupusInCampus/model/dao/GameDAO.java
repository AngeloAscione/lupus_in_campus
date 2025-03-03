package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.Game;
import NC12.LupusInCampus.model.Player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GameDAO extends JpaRepository<Game, Integer> {

    @Query(value = "SELECT p.* FROM partita p " +
            "JOIN listapartecipanti lp ON p.ID = lp.partitaID " +
            "WHERE lp.giocatoreID = :idPlayer", nativeQuery = true)
    List<Game> findGamesPartecipatedByIdPlayer(@Param("idPlayer") int id);

    @Query(value = "SELECT g.ID, g.nickname, g.email, g.pass, lp.ruolo " +
            "FROM listapartecipanti lp " +
            "JOIN partita p ON lp.partitaID = p.ID " +
            "JOIN giocatore g ON lp.giocatoreID = g.ID " +
            "WHERE lp.partitaID = :idGame", nativeQuery = true)
    List<Player> findPartecipantsByIdGame(@Param("idGame") int id);

    @Query(value = "SELECT lp.ruolo FROM listapartecipanti lp " +
            "JOIN partita p ON lp.partitaID = p.ID " +
            "JOIN giocatore g ON lp.giocatoreID = g.ID " +
            "WHERE g.ID = :idPlayer " +
            "AND lp.partitaID = :idGame", nativeQuery = true)
    String findRoleByPlayerId(@Param("idPlayer") int idPlayer, @Param("idGame") int idGame);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO listapartecipanti (partitaID, giocatoreID, ruolo) VALUES (:idGame, :idPlayer, :ruolo)", nativeQuery = true)
    void saveParticipants(@Param("idGame") int idGame, @Param("idPlayer") int idPlayer, @Param("ruolo") String ruolo);


    @Query(value = "SELECT p.* FROM partita p WHERE idCreatoreLobby = :creatorId ORDER BY dataPartita DESC LIMIT 1", nativeQuery = true)
    Game findLastCreatedGameByCreatorId(@Param("creatorId") int creatorId);


}
