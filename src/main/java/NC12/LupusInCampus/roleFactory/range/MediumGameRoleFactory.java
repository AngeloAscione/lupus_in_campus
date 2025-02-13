package NC12.LupusInCampus.roleFactory.range;

import NC12.LupusInCampus.model.enums.PlayerRole;
import NC12.LupusInCampus.roleFactory.RoleAssignmentFactory;

import java.util.ArrayList;
import java.util.List;

public class MediumGameRoleFactory implements RoleAssignmentFactory {
    @Override
    public List<PlayerRole> getRoles(int numPlayers) {

        List<PlayerRole> roles = new ArrayList<>(List.of(
                PlayerRole.STUDENT_OUT_COURSE,
                PlayerRole.STUDENT_OUT_COURSE,
                PlayerRole.RECTOR,
                PlayerRole.RESEARCHER,
                PlayerRole.GRADUATE
        ));

        if (numPlayers == 12) {
            roles.add(PlayerRole.GRADUATE);
        }

        return roles;
    }
}
