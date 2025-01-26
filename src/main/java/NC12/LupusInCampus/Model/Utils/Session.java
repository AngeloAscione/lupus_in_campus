package NC12.LupusInCampus.Model.Utils;

import jakarta.servlet.http.HttpSession;

public class Session {
    public Session(){}

    public static boolean sessionIsActive(HttpSession session) {
        return session.getAttribute("player") != null;
    }
}
