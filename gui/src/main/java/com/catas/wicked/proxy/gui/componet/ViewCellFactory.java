package com.catas.wicked.proxy.gui.componet;

import com.catas.wicked.proxy.service.RequestViewService;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ViewCellFactory {

    @Autowired
    private RequestViewService requestViewService;

    public RequestViewTreeCell<RequestCell> createTreeCell(TreeView<RequestCell> treeView) {
        // TODO
        RequestViewTreeCell<RequestCell> treeCell = new RequestViewTreeCell<>(treeView);
        treeCell.setRequestViewService(requestViewService);
        return treeCell;
    }

    public RequestViewListCell<RequestCell> createListCell(ListView<RequestCell> listView) {
        RequestViewListCell<RequestCell> listCell = new RequestViewListCell<>(listView);
        listCell.setRequestViewService(requestViewService);
        return listCell;
    }
}
