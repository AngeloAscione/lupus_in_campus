package NC12.LupusInCampus.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class Validator {
    private static final String EMAIL_REGEX = "[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}$";

    public Validator(){}

    public boolean emailIsValid(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return pattern.matcher(email).matches();
    }
}
