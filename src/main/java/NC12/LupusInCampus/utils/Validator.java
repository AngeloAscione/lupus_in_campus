package NC12.LupusInCampus.utils;

import java.util.regex.Pattern;

public class Validator {
    private static final String EMAIL_REGEX = "[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}$";

    public static boolean emailIsValid(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return pattern.matcher(email).matches();
    }
}
