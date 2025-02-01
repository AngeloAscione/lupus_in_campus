package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.LobbyInvitation;
import NC12.LupusInCampus.model.LobbyInvitationPk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LobbyInvitationDAO extends JpaRepository<LobbyInvitation, LobbyInvitationPk> {

}
