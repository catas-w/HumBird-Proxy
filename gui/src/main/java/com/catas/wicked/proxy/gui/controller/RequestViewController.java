package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.proxy.gui.componet.RequestCell;
import com.catas.wicked.proxy.gui.componet.RequestViewTreeCell;
import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
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
    private TreeItem root;

    public TreeItem getRoot() {
        return root;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // req-view
        filterInputEventBind();
        listViewEventBind(listViewMenuItem);
        listViewEventBind(treeViewMenuItem);

        reqTreeView.setCellFactory(view -> new RequestViewTreeCell<>(view));
        // reqTreeView.setCellFactory(new Callback<TreeView<RequestCell>, TreeCell<RequestCell>>() {
        //     @Override
        //     public TreeCell<RequestCell> call(TreeView<RequestCell> stringTreeView) {
        //         return null;
        //     }
        // });
    }


    private void listViewEventBind(MenuItem menuItem) {
        menuItem.setOnAction(e -> {
            FontIcon icon = (FontIcon) menuItem.getGraphic();
            FontIcon fontIcon = new FontIcon(icon.getIconCode());
            fontIcon.setIconSize(18);
            fontIcon.setIconColor(Color.web("#616161"));
            listViewMenuBtn.setGraphic(fontIcon);
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
