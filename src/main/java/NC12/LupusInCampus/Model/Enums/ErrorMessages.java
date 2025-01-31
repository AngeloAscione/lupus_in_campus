package NC12.LupusInCampus.Model.Enums;

public enum ErrorMessages {
    EMPTY_NICKNAME_FIELD(-1, "Il campo nickname non può essere vuoto"),
    EMPTY_EMAIL_FIELD(-1, "Il campo email non può essere vuoto"),
    EMPTY_PASSWORD_FIELD(-1, "Il campo password non può essere vuoto"),

    EMAIL_FORMAT(-1, "Email non corrisponde al formato"),

    EMAIL_ALREADY_USED(-1, "Email già in uso"),
    NICKNAME_ALREADY_USED(-1, "Nickname già in uso"),

    EMAIL_NOT_REGISTERED(-1, "Email non registrata"),

    INCORRECT_CREDENTIALS(-1, "Le credenziali non combaciano, riprova!"),
    EMPTY_ID(-1, "ID vuoto"),
    PLAYER_NOT_FOUND(-1, "Giocatore non trovato"),

    PLAYER_NOT_IN_SESSION(-1, "Giocatore nella sessione non trovato"),

    FRIEND_ALREADY_ADDED(-1, "Giocatore già presente nella lista amici"),

    FRIEND_NOT_DELETED(-1, "Eliminazione amico non riuscita"),

    PUBLIC_LOBBIES_NOT_FOUND(-1, "Lobby pubbliche attive non presenti"),
    LOBBY_NOT_FOUND(-1, "Lobby non trovata"),
    LIMIT_PLAYER_LOBBY(-1, "Lobby piena"),
    PLAYER_ALREADY_JOIN(-1, "Giocatore già presente nella lobby");

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
}
