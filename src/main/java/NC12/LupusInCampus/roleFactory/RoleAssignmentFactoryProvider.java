package NC12.LupusInCampus.roleFactory;

import NC12.LupusInCampus.roleFactory.range.LargeGameRoleFactory;
import NC12.LupusInCampus.roleFactory.range.MediumGameRoleFactory;
import NC12.LupusInCampus.roleFactory.range.SmallGameRoleFactory;
import NC12.LupusInCampus.utils.LoggerUtil;

public class RoleAssignmentFactoryProvider {

    public static RoleAssignmentFactory getFactory(int numPlayers) {
        if (numPlayers >= 6 && numPlayers <= 8) {
            return new SmallGameRoleFactory();
        } else if (numPlayers >= 9 && numPlayers <= 12) {
            return new MediumGameRoleFactory();
        } else if (numPlayers >= 13 && numPlayers <= 18) {
            return new LargeGameRoleFactory();
        } else {
           throw new IllegalArgumentException("Numero di giocatori non supportato");
        }
    }
}
