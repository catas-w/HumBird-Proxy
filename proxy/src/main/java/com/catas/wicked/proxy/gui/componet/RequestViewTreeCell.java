package com.catas.wicked.proxy.gui.componet;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.lang.ref.WeakReference;

public class RequestViewTreeCell<T> extends TreeCell<T> {

    private HBox hbox;
    private StackPane selectedPane = new StackPane();
    private Label methodLabel;
    private Label pathLabel;

    private InvalidationListener treeItemGraphicInvalidationListener = observable -> updateDisplay(getItem(),
            isEmpty());
    private WeakInvalidationListener weakTreeItemGraphicListener = new WeakInvalidationListener(
            treeItemGraphicInvalidationListener);

    private WeakReference<TreeItem<T>> treeItemRef;

    public RequestViewTreeCell() {
        selectedPane.getStyleClass().add("selection-bar");
        selectedPane.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        selectedPane.setPrefWidth(3);
        selectedPane.setMouseTransparent(true);
        selectedProperty().addListener((o, oldVal, newVal) -> selectedPane.setVisible(newVal ? true : false));

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
    }



    private void updateDisplay(T item, boolean empty) {
        if (item == null || empty) {
            hbox = null;
            setText(null);
            setGraphic(null);
        } else {
            TreeItem<T> treeItem = getTreeItem();
            if (treeItem != null && treeItem.getGraphic() != null) {
                if (item instanceof Node) {
                    setText(null);
                    if (hbox == null) {
                        hbox = new HBox(3);
                    }
                    hbox.getChildren().setAll(treeItem.getGraphic(), (Node) item);
                    setGraphic(hbox);
                } else {
                    hbox = null;
                    setText(item.toString());
                    treeItem.getGraphic().getStyleClass().add("req-method-label");
                    setGraphic(treeItem.getGraphic());
                }
            } else {
                hbox = null;
                if (item instanceof Node) {
                    setText(null);
                    setGraphic((Node) item);
                } else {
                    setText(item.toString());
                    if (hbox == null) {
                        hbox = new HBox(3);
                    }
                    if (methodLabel == null) {
                        methodLabel = new Label("POST");
                        methodLabel.getStyleClass().add("req-method-label");
                    }
                    hbox.getChildren().setAll(methodLabel);
                    setGraphic(hbox);
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
