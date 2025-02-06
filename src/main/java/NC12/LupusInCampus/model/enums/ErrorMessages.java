package NC12.LupusInCampus.model.enums;

public enum ErrorMessages {
    EMPTY_NICKNAME_FIELD(-1, "Il campo nickname non può essere vuoto"),
    EMPTY_EMAIL_FIELD(-2, "Il campo email non può essere vuoto"),
    EMPTY_PASSWORD_FIELD(-3, "Il campo password non può essere vuoto"),

    EMAIL_FORMAT(-4, "Email non corrisponde al formato"),

    EMAIL_ALREADY_USED(-5, "Email già in uso"),
    NICKNAME_ALREADY_USED(-6, "Nickname già in uso"),

    EMAIL_NOT_REGISTERED(-7, "Email non registrata"),

    INCORRECT_CREDENTIALS(-8, "Le credenziali non combaciano, riprova!"),
    EMPTY_ID(-9, "ID vuoto"),
    PLAYER_NOT_FOUND(-10, "Giocatore non trovato"),

    PLAYER_NOT_IN_SESSION(-11, "Giocatore nella sessione non trovato"),

    FRIEND_ALREADY_ADDED(-12, "Giocatore già presente nella lista amici"),

    FRIEND_NOT_DELETED(-13, "Eliminazione amico non riuscita"),

    PUBLIC_LOBBIES_NOT_FOUND(-14, "Lobby pubbliche attive non presenti"),
    LOBBY_NOT_FOUND(-15, "Lobby non trovata"),
    LIMIT_PLAYER_LOBBY(-16, "Lobby piena"),
    PLAYER_ALREADY_JOIN(-17, "Giocatore già presente nella lobby");

    private final String message;
    private final int code;

    ErrorMessages(int code, String message) {
        this.message = message;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "{\"ErrorMessages\":{" +
                "\"message\":\"" + message + '\"' +
                ", \"code\":" + code +
                "}}";
    }
}
