package NC12.LupusInCampus.model.enums;

public enum SuccessMessages {

    REGISTRATION_SUCCESS(0, "Registrazione avvenuta con successo."),
    LOGIN_SUCCESS(0, "Login effettuato con successo."),
    LOGOUT_SUCCESS(0, "Logout effettuato con successo."),

    PLAYER_DELETED(0, "Giocatore eliminato con successo"),
    PLAYER_ADDED_LOBBY(0, "Giocatore aggiunto nella lobby con successo"),
    PLAYER_REMOVED_LOBBY(0, "Giocatore rimosso correttamente dalla lobby"),

    FRIEND_DELETED(0, "Eliminazione amico riuscita"),
    FRIEND_ADDED(0, "Giocatore aggiunto alla lista amici"),
    FRIEND_LOADED(0, "Recuperata lista amici"),

    LOBBIES_LOADED(0, "Lobby pubbliche recuperate"),
    LOBBY_CREATED(0, "Lobby creata con successo"),
    LOBBY_DELETED(0, "Lobby eliminata con successo"),
    LOBBY_MODIFIED(0, "Lobby modificata con successo"),

    SEARCH(0, "Ricerca effettuata con successo"),
    LOAD_ALL_INFO(0, "Dati area utente caricati correttamente"),
    SUCCESS_EDIT(0, "Modifiche dei dati effettuate");

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
