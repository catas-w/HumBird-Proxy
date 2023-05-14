package com.catas.wicked.proxy.gui.componet;

import com.catas.wicked.proxy.service.RequestViewService;
import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

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
            if (this.requestCell != null && requestViewService != null) {
                // System.out.println("clicked list cell: " + requestCell.getPath());
                requestViewService.updateView(requestCell.getRequestId());
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

    private HBox createHBox(RequestCell cell) {
        HBox hBox = new HBox(3);
        if (cell.isOnCreated()) {
            triggerFade();
            // System.out.println("Refresh: created: " + requestCell.getPath());
        }
        if (this.requestCell == null) {
            this.requestCell = cell;
        }
        return hBox;
    }

    private void updateDisplay(T item, boolean empty) {
        if (item == null || empty) {
            hbox = null;
            setText(null);
            setGraphic(null);
        } else {
            T listItem = getItem();
            if (listItem != null) {
                if (item instanceof RequestCell) {
                    RequestCell requestCell = (RequestCell) item;
                    if (hbox == null) {
                        hbox = createHBox((RequestCell) item);
                        setText(((RequestCell) item).getPath());
                    }
                    if (methodLabel == null) {
                        methodLabel = new Label(requestCell.getMethod());
                        methodLabel.getStyleClass().add("req-method-label");
                        methodLabel.getStyleClass().add(requestCell.getStyleClass());
                    }
                    hbox.getChildren().setAll(methodLabel);
                    setGraphic(hbox);
                } else {
                    hbox = null;
                    setText(item.toString());
                }
            } else {
                hbox = null;
                methodLabel = null;
                if (item instanceof RequestCell) {
                    RequestCell requestCell = (RequestCell) item;
                    setText(requestCell.getPath());
                    hbox = createHBox(requestCell);
                    methodLabel = new Label(requestCell.getMethod());
                    methodLabel.getStyleClass().add("req-method-label");
                    methodLabel.getStyleClass().add(requestCell.getStyleClass());
                    hbox.getChildren().setAll(methodLabel);
                    setGraphic(hbox);
                } else {
                    setText(item.toString());
                    setGraphic(null);
                }
            }
        }
    }
}
