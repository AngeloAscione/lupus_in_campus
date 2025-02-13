package NC12.LupusInCampus.roleFactory.range;

import NC12.LupusInCampus.model.enums.PlayerRole;
import NC12.LupusInCampus.roleFactory.RoleAssignmentFactory;

import java.util.List;

public class LargeGameRoleFactory implements RoleAssignmentFactory {

    @Override
    public List<PlayerRole> getRoles(int numPlayers) {

        return List.of(
                PlayerRole.STUDENT_OUT_COURSE,
                PlayerRole.STUDENT_OUT_COURSE,
                PlayerRole.STUDENT_OUT_COURSE,
                PlayerRole.RECTOR,
                PlayerRole.RESEARCHER,
                PlayerRole.GRADUATE,
                PlayerRole.GRADUATE
        );
    }
}
