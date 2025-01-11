package NC12.LupusInCampus.Model.Utils;

public enum MessaggiErrore {
    CAMPO_NICKNAME_VUOTO(-1, "Il campo nickname non può essere vuoto"),
    CAMPO_EMAIL_VUOTO(-1,"Il campo email non può essere vuoto"),
    CAMPO_PASSWORD_VUOTO(-1,"Il campo password non può essere vuoto"),

    FORMATO_EMAIL(-1,"Email non corrisponde al formato"),

    EMAIL_GIA_USATA(-1,"Email già in uso"),
    NICKNAME_GIA_USATO(-1,"Nickname già in uso"),

    EMAIL_NON_REGISTRATA(-1,"Email non registrata"),

    CREDENZIALI_ERRATE(-1,"Le credenziali non combaciano, riprova!"),
    ID_VUOTO(-1,"ID vuoto"),
    GIOCATORE_INESISTENTE(-1,"Giocatore non trovato");

    private final String message;
    private final int codice;


    MessaggiErrore (int codice, String message) {
        this.message = message;
        this.codice = codice;
    }

    public int getCodice() {
        return codice;
    }

    public String getMessage() {
        return message;
    }
}
