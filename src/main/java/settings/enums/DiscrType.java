package settings.enums;

public enum  DiscrType {
    WEEK(0, "7 days"),
    MONTH(1, "30 days");

    DiscrType(int id, String textValue) {
        this.id = id;
        this.textValue = textValue;
    }

    private final int id;
    private final String textValue;

    public int getId() {
        return id;
    }

    public String getTextValue() {
        return textValue;
    }
}
