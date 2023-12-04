package com.catas.wicked.proxy.gui.componet;

import com.catas.wicked.common.bean.RequestCell;
import com.catas.wicked.proxy.service.RequestViewService;
import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

public class RequestViewListCell<T> extends ListCell<T> {

    private HBox hbox;
    private StackPane selectedPane = new StackPane();
    private Label methodLabel;
    private FadeTransition fadeTransition;
    private FadeTransition showTransition;
    private RequestCell requestCell;
    private RequestViewService requestViewService;
    private final static String DEFAULT_STYLE_CLASS = "req-list-cell";

    public RequestViewListCell(ListView<RequestCell> listView) {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        selectedPane.getStyleClass().add("req-cell-bar");
        selectedPane.setMouseTransparent(true);

        this.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && this.requestCell != null && requestViewService != null) {
                System.out.println("clicked list: " + requestCell.getFullPath() + " " + requestCell.getRequestId());
                requestViewService.updateRequestTab(requestCell.getRequestId());
            }
        });
    }

    public void setRequestViewService(RequestViewService requestViewService) {
        this.requestViewService = requestViewService;
    }

    /**
     * play animation
     */
    private void triggerFade() {
        if (showTransition == null) {
            showTransition = new FadeTransition();
            showTransition.setNode(selectedPane);
            showTransition.setDuration(Duration.millis(500));
            showTransition.setCycleCount(1);
            showTransition.setAutoReverse(true);
            showTransition.setFromValue(0);
            showTransition.setToValue(1);
        }
        if (fadeTransition == null) {
            fadeTransition = new FadeTransition();
            fadeTransition.setNode(selectedPane);
            fadeTransition.setDuration(Duration.millis(1000));
            fadeTransition.setCycleCount(1);
            fadeTransition.setAutoReverse(true);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
        }
        showTransition.play();
        fadeTransition.play();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (!getChildren().contains(selectedPane)) {
            getChildren().add(0, selectedPane);
        }
        selectedPane.resizeRelocate(0, 0, getWidth(), getHeight());
        selectedPane.setVisible(true);
        selectedPane.setOpacity(0);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        updateDisplay(item, empty);
        setMouseTransparent(item == null || empty);
    }

    private void createHBox(RequestCell cell) {
        hbox = new HBox(3);
        if (cell.isOnCreated()) {
            triggerFade();
        }
        if (this.requestCell == null) {
            this.requestCell = cell;
        }
    }

    private void updateDisplay(T item, boolean empty) {
        if (item == null || empty) {
            // hbox = null;
            setText(null);
            setGraphic(null);
        } else {
            if (item instanceof RequestCell requestCell) {
                if (methodLabel == null) {
                    methodLabel = new Label(requestCell.getMethod());
                    methodLabel.getStyleClass().add("req-method-label");
                    methodLabel.getStyleClass().add(requestCell.getStyleClass());
                } else {
                    if (!StringUtils.equals(requestCell.getMethod(), methodLabel.getText())) {
                        methodLabel.setText(requestCell.getMethod());
                    }
                    if (!methodLabel.getStyleClass().contains(requestCell.getStyleClass())) {
                        methodLabel.getStyleClass().removeIf(styleClass -> styleClass.startsWith("method-label"));
                        methodLabel.getStyleClass().add(requestCell.getStyleClass());
                    }
                }

                if (hbox == null) {
                    createHBox(requestCell);
                    hbox.getChildren().setAll(methodLabel);
                }
                setText(requestCell.getPath());
                setGraphic(hbox);
            }
        }
    }
}
