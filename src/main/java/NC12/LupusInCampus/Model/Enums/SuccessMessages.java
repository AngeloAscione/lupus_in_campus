package NC12.LupusInCampus.Model.Enums;

public enum SuccessMessages {

    REGISTRATION_SUCCESS(0, "Registrazione avvenuta con successo."),
    LOGIN_SUCCESS(0, "Login effettuato con successo."),

    PLAYER_DELETED(0, "Giocatore eliminato con successo"),

    FRIEND_DELETED(0, "Eliminazione amico riuscita"),
    FRIEND_ADDED(0, "Giocatore aggiunto alla lista amici"),
    FRIEND_LOADED(0, "Recuperata lista amici");

    private final String message;
    private final int code;

    SuccessMessages(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
