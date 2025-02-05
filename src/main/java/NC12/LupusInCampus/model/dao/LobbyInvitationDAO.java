package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.LobbyInvitation;
import NC12.LupusInCampus.model.LobbyInvitationPk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LobbyInvitationDAO extends JpaRepository<LobbyInvitation, LobbyInvitationPk> {

    List<LobbyInvitation> findLobbyInvitationsByLobbyInvitationPkInvitedPlayerId(int invitedPlayerId);
}
