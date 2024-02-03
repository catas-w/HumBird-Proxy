package com.catas.wicked.proxy.gui.controller;

import com.jfoenix.controls.JFXButton;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

@Slf4j
@Singleton
public class SettingController implements Initializable {

    @FXML
    private TextField portField;
    @FXML
    private TextField maxSizeField;
    @FXML
    private JFXButton saveBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };

        portField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 9624, integerFilter));
        maxSizeField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 10, integerFilter));
    }

    public void save() {
        System.out.println("Click save!");
    }
}
