package com.catas.wicked.proxy.gui.componet;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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

    private InvalidationListener treeItemGraphicInvalidationListener = observable -> updateDisplay(getItem(),
            isEmpty());
    private WeakInvalidationListener weakTreeItemGraphicListener = new WeakInvalidationListener(
            treeItemGraphicInvalidationListener);

    private WeakReference<TreeItem<T>> treeItemRef;

    public RequestViewTreeCell(TreeView<RequestCell> treeView) {
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

        this.setOnMouseClicked(e -> {
            TreeItem<T> treeItem = getTreeItem();
            if (treeItem != null) {
                RequestCell cell = (RequestCell) treeItem.getValue();
                System.out.println("Clicked " + cell.getPath());
            }
        });
    }

    private HBox createHBox(RequestCell requestCell) {
        HBox hBox = new HBox(3);
        if (requestCell.isLeaf()) {
            hBox.getStyleClass().add("req-leaf");
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
                if (item instanceof Node) {
                    setText(null);
                    if (hbox == null) {
                        hbox = new HBox(3);
                    }
                    hbox.getChildren().setAll(treeItem.getGraphic(), (Node) item);
                    setGraphic(hbox);
                } else if (item instanceof RequestCell) {
                    if (hbox == null) {
                        hbox = createHBox((RequestCell) item);
                        setText(((RequestCell) item).getPath());
                        treeItem.getGraphic().getStyleClass().add("req-method-label");
                        hbox.getChildren().setAll(treeItem.getGraphic());
                    }
                    setGraphic(hbox);
                } else {
                    hbox = null;
                    setText(item.toString());
                    setGraphic(treeItem.getGraphic());
                }
            } else {
                hbox = null;
                methodLabel = null;
                if (item instanceof Node) {
                    setText(null);
                    setGraphic((Node) item);
                } else if (item instanceof RequestCell) {
                    RequestCell requestCell = (RequestCell) item;
                    setText(requestCell.getPath());
                    if (hbox == null) {
                        hbox = createHBox(requestCell);
                    }
                    if (methodLabel == null) {
                        methodLabel = new Label(requestCell.getMethod());
                        methodLabel.getStyleClass().add("req-method-label");
                        methodLabel.getStyleClass().add(requestCell.getStyleClass());
                    }
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
