package com.catas.wicked.proxy.gui.componet;

import com.catas.wicked.common.bean.RequestCell;
import com.catas.wicked.proxy.service.RequestViewService;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.lang.ref.WeakReference;

public class RequestViewTreeCell<T> extends TreeCell<T> {

    private HBox hbox;
    private StackPane selectedPane = new StackPane();
    private Label methodLabel;
    private FadeTransition fadeTransition;
    private FadeTransition showTransition;
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
            if (e.getButton() == MouseButton.PRIMARY && treeItem != null && requestViewService != null) {
                RequestCell cell = (RequestCell) treeItem.getValue();
                // System.out.println("Clicked " + cell.getFullPath() + " " + cell.getRequestId() + "isLeaf: " + cell.isLeaf());
                if (cell.isLeaf()) {
                    requestViewService.updateRequestTab(cell.getRequestId());
                }
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

    private void createOrUpdateHBox(RequestCell requestCell) {
        hbox = new HBox(3);

        if (requestCell.isLeaf()) {
            hbox.getStyleClass().add("req-leaf");
        } else {
            FontIcon icon = new FontIcon();
            icon.getStyleClass().add("req-icon");
            icon.setIconColor(Color.valueOf("#8C9C9E"));
            if (requestCell.getPath().startsWith("http")) {
                icon.setIconLiteral("fas-globe-africa");
            } else {
                icon.setIconLiteral("fas-folder-minus");
            }
            icon.setIconSize(14);
            // icon.getStyleClass().add("request-path-icon");
            hbox.getChildren().add(icon);
        }
        if (requestCell.isOnCreated()) {
            triggerFade();
        }
        // return box;
    }

    private void updateDisplay(T item, boolean empty) {
        if (item == null || empty) {
            hbox = null;
            setText(null);
            setGraphic(null);
            if (this.showTransition != null) {
                this.showTransition.stop();
            }
            if (this.fadeTransition != null) {
                this.fadeTransition.stop();
            }
            selectedPane.setVisible(false);
        } else {
            if (item instanceof RequestCell requestCell) {
                setText(requestCell.getPath());
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
                    createOrUpdateHBox(requestCell);
                    hbox.getChildren().add(methodLabel);
                }
                setGraphic(hbox);
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
