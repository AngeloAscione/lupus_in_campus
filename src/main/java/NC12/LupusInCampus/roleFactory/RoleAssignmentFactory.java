package NC12.LupusInCampus.roleFactory;

import NC12.LupusInCampus.model.enums.PlayerRole;

import java.util.List;

public interface RoleAssignmentFactory {
    List<PlayerRole> getRoles(int numPlayers);
}
