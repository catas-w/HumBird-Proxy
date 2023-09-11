package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.proxy.gui.componet.RequestCell;
import com.catas.wicked.proxy.gui.componet.ViewCellFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class RequestViewController implements Initializable {

    @FXML
    private MenuButton listViewMenuBtn;
    @FXML
    private MenuItem treeViewMenuItem;
    @FXML
    private MenuItem listViewMenuItem;
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

    @Inject
    private ViewCellFactory cellFactory;

    public TreeItem getTreeRoot() {
        return root;
    }

    public ListView<RequestCell> getReqListView() {
        return reqListView;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // req-view
        filterInputEventBind();
        listViewEventBind(listViewMenuItem);
        listViewEventBind(treeViewMenuItem);

        reqTreeView.setCellFactory(treeView -> cellFactory.createTreeCell(treeView));
        reqListView.setCellFactory(listView -> cellFactory.createListCell(listView));
    }


    private void listViewEventBind(MenuItem menuItem) {
        menuItem.setOnAction(e -> {
            FontIcon icon = (FontIcon) menuItem.getGraphic();
            FontIcon fontIcon = new FontIcon(icon.getIconCode());
            fontIcon.setIconSize(18);
            fontIcon.setIconColor(Color.web("#616161"));
            listViewMenuBtn.setGraphic(fontIcon);

            if (menuItem.getId().contains("list")) {
                reqTreeView.setVisible(false);
                reqListView.setVisible(true);
            } else {
                reqTreeView.setVisible(true);
                reqListView.setVisible(false);
            }
        });
    }

    private void filterInputEventBind() {
        filterInput.setOnKeyTyped(e -> {
            CharSequence characters = filterInput.getText();
            if (characters.length() > 0) {
                filterCancelBtn.setVisible(true);
            } else {
                filterCancelBtn.setVisible(false);
            }
        });

        filterCancelBtn.setOnMouseClicked(e -> {
            filterInput.clear();
            filterCancelBtn.setVisible(false);
        });
    }

}
