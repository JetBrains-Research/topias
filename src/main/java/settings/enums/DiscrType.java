package settings.enums;

import com.intellij.util.xmlb.annotations.Attribute;

import java.util.Arrays;

public enum  DiscrType {
    @Attribute
    WEEK(0, "7 days"),
    @Attribute
    MONTH(1, "30 days");

    DiscrType(int id, String textValue) {
        this.id = id;
        this.textValue = textValue;
    }

    public static DiscrType getById(int id) {
        return Arrays.stream(values()).filter(x -> x.id == id).findFirst().orElse(DiscrType.MONTH);
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

    public void setId(int id) {
        this.id = id;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }
}
