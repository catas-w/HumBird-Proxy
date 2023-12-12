package com.catas.wicked.proxy.gui.componet.highlight;

import com.catas.wicked.common.constant.CodeStyle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

public class CodeStyleLabel extends Label implements CodeStyleLabeled {

    private StringProperty codeStyle = new SimpleStringProperty(CodeStyle.ORIGIN.name());

    public CodeStyleLabel(String text) {
        super(text);
    }

    public CodeStyleLabel(String text, CodeStyle codeStyle) {
        this(text, codeStyle.name());
    }

    public CodeStyleLabel(String text, String codeStyle) {
        super(text);
        this.setCodeStyle(codeStyle);
    }

    @Override
    public CodeStyle targetCodeStyle() {
        return CodeStyle.valueOfIgnoreCase(this.getCodeStyle());
    }

    public String getCodeStyle() {
        return codeStyle.get();
    }

    public StringProperty codeStyleProperty() {
        return codeStyle;
    }

    public void setCodeStyle(String codeStyle) {
        this.codeStyle.set(codeStyle);
    }
}
