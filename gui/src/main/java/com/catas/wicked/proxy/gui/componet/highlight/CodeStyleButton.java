package com.catas.wicked.proxy.gui.componet.highlight;

import com.catas.wicked.common.constant.CodeStyle;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CodeStyleButton extends JFXButton implements CodeStyleLabeled {

    private StringProperty codeStyle = new SimpleStringProperty(CodeStyle.ORIGIN.name());

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
