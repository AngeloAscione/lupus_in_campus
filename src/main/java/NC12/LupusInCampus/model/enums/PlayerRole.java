package NC12.LupusInCampus.model.enums;


public enum PlayerRole {
    STUDENT_OUT_COURSE("Studente fuori corso"),
    STUDENT_IN_COURSE("Studente in corso"),
    RECTOR("Rettore"),
    RESEARCHER("Ricercatore"),
    GRADUATE("Laureando");

    private final String text;

    PlayerRole(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
