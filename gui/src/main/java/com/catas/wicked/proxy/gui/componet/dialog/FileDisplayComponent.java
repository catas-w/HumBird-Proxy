package com.catas.wicked.proxy.gui.componet.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;


@Slf4j
public class FileDisplayComponent extends HBox {

    private static final String STYLE_CLASS = "file-display";
    private final Label label;
    private final Pane pane;
    private final Button button;

    public FileDisplayComponent(String fileName) {
        // to display fileName
        label = new Label(fileName);
        FontIcon labelIcon = new FontIcon();
        labelIcon.setIconLiteral("fas-link");
        label.setGraphic(labelIcon);
        label.prefHeightProperty().bind(this.heightProperty());
        HBox.setMargin(label, new Insets(0, 0, 0, 10));

        Tooltip labelTooltip = new Tooltip();
        labelTooltip.setShowDelay(Duration.millis(100));
        label.setTooltip(labelTooltip);

        pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);

        // hide-self button
        button = new Button();
        FontIcon icon = new FontIcon();
        icon.setIconLiteral("fas-times");
        button.setGraphic(icon);
        button.prefHeightProperty().bind(this.heightProperty());
        button.setOnAction(event -> this.setVisible(false));

        Tooltip tooltip = new Tooltip("Remove");
        tooltip.setShowDelay(Duration.millis(100));
        button.setTooltip(tooltip);

        getStyleClass().add(STYLE_CLASS);
        getChildren().addAll(label, pane, button);
    }

    public void setDisplay(String text) {
        if (text != null && text.length() > 16) {
            label.setText(text.substring(0, 15) + "...");
        } else {
            label.setText(text);
        }
        label.getTooltip().setText(text);
    }
}
