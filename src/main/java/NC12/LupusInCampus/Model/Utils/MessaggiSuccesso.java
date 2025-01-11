package NC12.LupusInCampus.Model.Utils;

public enum MessaggiSuccesso {

    REGISTRAZIONE_RIUSCITA(0,"Registrazione avvenuta con successo."),
    LOGIN_RIUSCITO(0,"Login effettuato con successo."),

    GIOCATORE_ELIMINATO(0,"Giocatore eliminato con successo");

    private final String message;
    private final int codice;

    MessaggiSuccesso(int codice, String message) {
        this.codice = codice;
        this.message = message;
    }

    public int getCodice() {
        return codice;
    }

    public String getMessage() {
        return message;
    }
}
