package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.bean.RequestCell;
import com.catas.wicked.common.bean.message.DeleteMessage;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.pipeline.Topic;
import com.catas.wicked.proxy.gui.componet.FilterableTreeItem;
import com.catas.wicked.proxy.gui.componet.TreeItemPredicate;
import com.catas.wicked.proxy.gui.componet.ViewCellFactory;
import com.catas.wicked.proxy.message.MessageService;
import com.catas.wicked.proxy.service.RequestViewService;
import com.jfoenix.controls.JFXToggleNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@Slf4j
@Singleton
public class RequestViewController implements Initializable {

    @FXML
    public JFXToggleNode viewToggleNode;
    @FXML
    private TextField filterInput;
    @FXML
    private Button filterCancelBtn;
    @FXML
    private TreeView<RequestCell> reqTreeView;
    @FXML
    private ListView<RequestCell> reqListView;
    @FXML
    private TreeItem root;
    @FXML
    private ContextMenu contextMenu;
    @Inject
    private ViewCellFactory cellFactory;
    @Inject
    private MessageQueue messageQueue;
    @Inject
    private RequestViewService requestViewService;

    private MessageService messageService;
    /**
     * save requestList in filteredList
     */
    private ObservableList<RequestCell> reqSourceList;
    private FilteredList<RequestCell> filteredList;

    public FilterableTreeItem getTreeRoot() {
        return (FilterableTreeItem) reqTreeView.getRoot();
    }

    public ListView<RequestCell> getReqListView() {
        return reqListView;
    }

    public TreeView<RequestCell> getReqTreeView() {
        return reqTreeView;
    }

    public ObservableList<RequestCell> getReqSourceList() {
        return reqSourceList;
    }

    /**
     * To avoid circular dependency
     * Since postConstruct() is executed earlier than initialize()
     */
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reqTreeView.setRoot(new FilterableTreeItem<>());

        // make reqListView filterable
        reqSourceList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(reqSourceList);
        reqListView.setItems(filteredList);

        // init filterTextField
        filterInputEventBind();

        reqTreeView.setCellFactory(treeView -> cellFactory.createTreeCell(treeView));
        reqListView.setCellFactory(listView -> cellFactory.createListCell(listView));

        reqTreeView.setContextMenu(contextMenu);
        reqListView.setContextMenu(contextMenu);

        reqTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            RequestCell requestCell = newValue.getValue();
            if (!requestCell.isLeaf()) {
                // TODO display pathNode
                return;
            }
            String requestId = requestCell.getRequestId();
            requestViewService.updateRequestTab(requestId);
            messageService.selectRequestItem(requestId, true);
        });
        reqListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            String requestId = newValue.getRequestId();
            requestViewService.updateRequestTab(requestId);
            messageService.selectRequestItem(requestId, false);
        }));

        toggleRequestView();
    }

    public void toggleRequestView() {
        viewToggleNode.selectedProperty().addListener((observable, oldValue, newValue) -> {
            FontIcon icon = (FontIcon) viewToggleNode.getGraphic();
            if (newValue) {
                icon.setIconLiteral("fas-list-ul");
            } else {
                icon.setIconLiteral("fas-tree");
            }
            reqTreeView.setVisible(!newValue);
            reqListView.setVisible(newValue);
        });
    }

    /**
     * filter requests
     */
    private void filterInputEventBind() {
        filterCancelBtn.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> !filterInput.getText().isEmpty(), filterInput.textProperty()));

        filterCancelBtn.setOnAction(e -> {
            filterInput.clear();
        });

        // bind filter treeView from: JFX
        getTreeRoot().predicateProperty().bind(Bindings.createObjectBinding(() -> {
            // System.out.println(filterInput.getText());
            if (filterInput.getText() == null || filterInput.getText().isEmpty())
                return null;
            return TreeItemPredicate.create(actor -> actor.toString().contains(filterInput.getText()));
        }, filterInput.textProperty()));

        // bind filter listView
        filteredList.predicateProperty().bind(Bindings.createObjectBinding(new Callable<Predicate<? super RequestCell>>() {
            @Override
            public Predicate<? super RequestCell> call() throws Exception {
                if (filterInput.getText() == null || filterInput.getText().isEmpty())
                    return null;
                return new Predicate<RequestCell>() {
                    @Override
                    public boolean test(RequestCell requestCell) {
                        return requestCell.getFullPath().contains(filterInput.getText());
                    }
                };
            }
        }, filterInput.textProperty()));
    }

    /**
     * remove item from listView or treeView
     */
    public void removeItem(ActionEvent event) {
        TreeItem<RequestCell> selectedItem = null;
        RequestCell requestCell = null;
        DeleteMessage deleteMessage = new DeleteMessage();

        if (!viewToggleNode.selectedProperty().get()) {
            // from tree view
            selectedItem = reqTreeView.getSelectionModel().getSelectedItem();
            FilterableTreeItem<RequestCell> parent = (FilterableTreeItem<RequestCell>) selectedItem.getParent();
            parent.getInternalChildren().remove(selectedItem);
            requestCell = selectedItem.getValue();
            deleteMessage.setSource(DeleteMessage.Source.TREE_VIEW);
        } else {
            // from list view
            requestCell = reqListView.getSelectionModel().getSelectedItem();
            // reqListView.getItems().remove(requestCell);
            reqSourceList.remove(requestCell);
            deleteMessage.setSource(DeleteMessage.Source.LIST_VIEW);
        }

        if (requestCell == null) {
            log.error("Unable to delete request, request cell is null.");
        }
        deleteMessage.setRequestCell(requestCell);
        messageQueue.pushMsg(Topic.RECORD, deleteMessage);
    }

    /**
     * clear all leaf-nodes of treeView
     */
    public void clearLeafNode() {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setCleanLeaves(true);
        messageQueue.pushMsg(Topic.RECORD, deleteMessage);
    }
}
