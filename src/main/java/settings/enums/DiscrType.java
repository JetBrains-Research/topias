package settings.enums;

import com.intellij.util.xmlb.annotations.Attribute;


public enum  DiscrType {
    WEEK(0, "Week"),
    MONTH(1, "Month");

    DiscrType(int id, String textValue) {
        this.id = id;
        this.textValue = textValue;
    }

    @Attribute
    public int id;
    @Attribute
    public String textValue;

    public int getId() {
        return id;
    }

    public String getTextValue() {
        return textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }
}
