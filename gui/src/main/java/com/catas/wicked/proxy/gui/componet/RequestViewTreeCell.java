package com.catas.wicked.proxy.gui.componet;

import com.catas.wicked.proxy.service.RequestViewService;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.lang.ref.WeakReference;

public class RequestViewTreeCell<T> extends TreeCell<T> {

    private HBox hbox;
    private StackPane selectedPane = new StackPane();
    private Label methodLabel;
    private FadeTransition fadeTransition;
    private FadeTransition showTransition;
    private RequestCell requestCell;
    private RequestViewService requestViewService;

    private InvalidationListener treeItemGraphicInvalidationListener = observable -> updateDisplay(getItem(),
            isEmpty());
    private WeakInvalidationListener weakTreeItemGraphicListener = new WeakInvalidationListener(
            treeItemGraphicInvalidationListener);

    private WeakReference<TreeItem<T>> treeItemRef;

    public RequestViewTreeCell(TreeView<RequestCell> treeView) {
        selectedPane.getStyleClass().add("req-cell-bar");
        selectedPane.setMouseTransparent(true);

        final InvalidationListener treeItemInvalidationListener = observable -> {
            TreeItem<T> oldTreeItem = treeItemRef == null ? null : treeItemRef.get();
            if (oldTreeItem != null) {
                oldTreeItem.graphicProperty().removeListener(weakTreeItemGraphicListener);
            }

            TreeItem<T> newTreeItem = getTreeItem();
            if (newTreeItem != null) {
                newTreeItem.graphicProperty().addListener(weakTreeItemGraphicListener);
                treeItemRef = new WeakReference<>(newTreeItem);
            }
        };
        final WeakInvalidationListener weakTreeItemListener = new WeakInvalidationListener(treeItemInvalidationListener);
        treeItemProperty().addListener(weakTreeItemListener);
        if (getTreeItem() != null) {
            getTreeItem().graphicProperty().addListener(weakTreeItemGraphicListener);
        }

        this.setOnMouseClicked(e -> {
            TreeItem<T> treeItem = getTreeItem();
            if (treeItem != null && requestViewService != null) {
                RequestCell cell = (RequestCell) treeItem.getValue();
                // System.out.println("Clicked " + cell.getPath());
                requestViewService.updateView(cell.getRequestId());
            }
        });
    }

    public void setRequestViewService(RequestViewService requestViewService) {
        this.requestViewService = requestViewService;
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
        if (this.fadeTransition == null) {
            this.fadeTransition = new FadeTransition();
            this.fadeTransition.setNode(selectedPane);
            this.fadeTransition.setDuration(Duration.millis(1000));
            this.fadeTransition.setCycleCount(1);
            this.fadeTransition.setAutoReverse(true);
            this.fadeTransition.setFromValue(1.0);
            this.fadeTransition.setToValue(0.0);
        }
        showTransition.play();
        this.fadeTransition.play();
    }

    private HBox createHBox(RequestCell requestCell) {
        HBox hBox = new HBox(3);
        if (requestCell.isLeaf()) {
            hBox.getStyleClass().add("req-leaf");
        }
        if (requestCell.isOnCreated()) {
            triggerFade();
            // System.out.println("Refresh: created: " + requestCell.getPath());
        }
        return hBox;
    }

    private void updateDisplay(T item, boolean empty) {
        if (item == null || empty) {
            hbox = null;
            setText(null);
            setGraphic(null);
        } else {
            TreeItem<T> treeItem = getTreeItem();
            if (treeItem != null && treeItem.getGraphic() != null) {
                // System.out.println("1111");
                if (item instanceof RequestCell) {
                    if (hbox == null) {
                        hbox = createHBox((RequestCell) item);
                        setText(((RequestCell) item).getPath());
                        treeItem.getGraphic().getStyleClass().add("req-method-label");
                        hbox.getChildren().setAll(treeItem.getGraphic());
                        setGraphic(hbox);
                    }
                } else {
                    hbox = null;
                    setText(item.toString());
                    setGraphic(treeItem.getGraphic());
                }
            } else {
                // System.out.println("2222");
                hbox = null;
                methodLabel = null;
                if (item instanceof RequestCell) {
                    RequestCell requestCell = (RequestCell) item;
                    setText(requestCell.getPath());
                    methodLabel = new Label(requestCell.getMethod());
                    methodLabel.getStyleClass().add("req-method-label");
                    methodLabel.getStyleClass().add(requestCell.getStyleClass());
                    hbox = createHBox(requestCell);
                    hbox.getChildren().setAll(methodLabel);
                    setGraphic(hbox);
                } else {
                    setText(item.toString());
                    setGraphic(null);
                }
            }
        }
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        updateDisplay(item, empty);
        setMouseTransparent(item == null || empty);
    }
}
