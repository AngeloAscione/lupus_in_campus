package NC12.LupusInCampus.model.enums;

public enum SuccessMessages {

    REGISTRATION_SUCCESS(1, "Registrazione avvenuta con successo."),
    LOGIN_SUCCESS(2, "Login effettuato con successo."),
    LOGOUT_SUCCESS(3, "Logout effettuato con successo."),

    PLAYER_DELETED(4, "Giocatore eliminato con successo"),
    PLAYER_ADDED_LOBBY(5, "Giocatore aggiunto nella lobby con successo"),
    PLAYER_REMOVED_LOBBY(6, "Giocatore rimosso correttamente dalla lobby"),

    FRIEND_DELETED(7, "Eliminazione amico riuscita"),
    FRIEND_ADDED(8, "Giocatore aggiunto alla lista amici"),
    FRIEND_LOADED(9, "Recuperata lista amici"),

    LOBBIES_LOADED(10, "Lobby pubbliche recuperate"),
    LOBBY_CREATED(11, "Lobby creata con successo"),
    LOBBY_DELETED(12, "Lobby eliminata con successo"),
    LOBBY_MODIFIED(13, "Lobby modificata con successo"),

    SEARCH(14, "Ricerca effettuata con successo"),
    LOAD_ALL_INFO(15, "Dati area utente caricati correttamente"),
    SUCCESS_EDIT(16, "Modifiche dei dati effettuate"),

    NOTIFICATION_SENT(17, "Notifica inviata"),

    EMAIL_SENT(18, "Email inviata"),
    PASSWORD_RESET(19, "Password reimpostata correttamente"),;

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

    @Override
    public String toString() {
        return "{\"Enum\":{" +
                "\"message\":\"" + message + '\"' +
                ", \"code\":" + code +
                "}}";
    }
}
