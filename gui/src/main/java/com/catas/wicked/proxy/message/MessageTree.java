package com.catas.wicked.proxy.message;

import com.catas.wicked.common.bean.BaseMessage;
import com.catas.wicked.common.bean.PoisonMessage;
import com.catas.wicked.common.bean.RequestMessage;
import com.catas.wicked.common.bean.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.proxy.gui.componet.RequestCell;
import com.catas.wicked.proxy.gui.controller.RequestViewController;
import com.catas.wicked.common.util.ThreadPoolService;
import com.catas.wicked.common.util.WebUtils;
import io.netty.handler.codec.http.HttpMethod;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class MessageTree {

    private final TreeNode root = new TreeNode();

    private TreeNode latestNode;

    private Thread worker;

    @Autowired
    private ApplicationConfig appConfig;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private RequestViewController requestViewController;

    @Autowired
    private Cache<String, RequestMessage> requestCache;

    @PostConstruct
    public void init() {
        // fetch data from queue and add to message tree
        latestNode = root;
        worker = new Thread(() -> {
            while (!appConfig.getShutDownFlag().get()) {
                try {
                    BaseMessage msg = messageQueue.getMsg();
                    processMsg(msg);
                } catch (InterruptedException e) {
                    log.info("-- quit --");
                    break;
                } catch (Exception e) {
                    log.error("Error occurred in message tree thread", e);
                }
            }
        });
        ThreadPoolService.getInstance().run(worker);
    }

    private void processMsg(BaseMessage msg) throws Exception {
        if (msg instanceof PoisonMessage) {
            throw new InterruptedException();
        }
        if (msg.getType() == BaseMessage.MessageType.REQUEST) {
            add((RequestMessage) msg);
        } else if (msg.getType() == BaseMessage.MessageType.RESPONSE) {
            ResponseMessage respMessage = (ResponseMessage) msg;
            RequestMessage data = requestCache.get(respMessage.getRequestId());
            if (data != null) {
                System.out.println("<<<<< Add Resp >>>>>" + respMessage.getRequestId());
                data.setResponse(respMessage);
                requestCache.put(data.getRequestId(), data);
            }
        }
    }

    /**
     * 根据 path 添加节点到树中
     * @param msg request/response entity
     */
    private void add(RequestMessage msg) {
        // put to cache
        requestCache.put(msg.getRequestId(), msg);

        // create leaf node
        TreeNode node = new TreeNode();
        node.setRequestId(msg.getRequestId());
        node.setMethod(new HttpMethod(msg.getMethod()));
        node.setUrl(msg.getRequestUrl());
        node.setType(msg.getType().name());
        node.setLeaf(true);

        // add node to its position
        List<String> pathSplits = WebUtils.getPathSplits(msg.getUrl().toString());
        node.setPath(pathSplits.get(pathSplits.size() - 1));
        pathSplits.remove(pathSplits.size() - 1);

        TreeNode parent = findAndCreatParentNode(root, pathSplits, 0);
        parent.getRequestList().add(node);

        // 创建 UI
        createTreeItemUI(parent, node);
        createListItemUI(node);

        latestNode.setNext(node);
        node.setPrev(latestNode);
        latestNode = node;
    }

    /**
     * 创建 tree item
     * @param parent parent node
     * @param node tree node
     */
    private void createTreeItemUI(TreeNode parent, TreeNode node) {
        if (!node.isLeaf() && node.getTreeItem() != null) {
            return;
        }
        if (parent == root) {
            parent.setTreeItem(requestViewController.getRoot());
        }
        TreeItem<RequestCell> parentTreeItem = parent.getTreeItem();
        TreeItem<RequestCell> treeItem = new TreeItem<>();

        RequestCell requestCell = new RequestCell(node.getPath(), node.getMethod() == null ? "": node.getMethod().name());
        requestCell.setLeaf(node.isLeaf());
        treeItem.setValue(requestCell);

        if (!node.isLeaf() && node.getPath().startsWith("http")) {
            FontIcon icon = new FontIcon();
            icon.setIconColor(Color.valueOf("#1D78C6"));
            icon.setIconLiteral("fas-globe-africa");
            icon.setIconSize(14);
            treeItem.setGraphic(icon);
        } else if (!node.isLeaf()) {
            FontIcon icon = new FontIcon();
            icon.setIconColor(Color.valueOf("#1D78C6"));
            icon.setIconLiteral("fas-tag");
            icon.setIconSize(14);
            treeItem.setGraphic(icon);
        }
        node.setTreeItem(treeItem);
        node.setCreatedUI(true);
        Platform.runLater(() -> {
           parentTreeItem.getChildren().add(treeItem);
        });
    }

    /**
     * 根据 path 查找并创建需要添加的父节点
     * @param parent parent node
     * @param index index
     */
    private TreeNode findAndCreatParentNode(TreeNode parent, List<String> path, int index) {
        if (index >= path.size()) {
            return parent;
        }
        String curPath = path.get(index);
        TreeNode node = parent.getChildren().get(curPath);
        if (node == null) {
            node = new TreeNode();
            node.setPath(curPath);
            parent.getChildren().put(curPath, node);
        }
        createTreeItemUI(parent, node);
        RequestCell cell = node.getTreeItem().getValue();
        cell.setCreatedTime(System.currentTimeMillis());
        return findAndCreatParentNode(node, path, ++index);
    }

    /**
     * 创建 list-item ui
     * @param node
     */
    private void createListItemUI(TreeNode node) {
        if (node == null || !node.isLeaf()) {
            return;
        }
        RequestCell requestCell = new RequestCell(node.getUrl(), node.getMethod() == null ? "" : node.getMethod().name());
        ListView<RequestCell> reqListView = requestViewController.getReqListView();
        Platform.runLater(() -> {
            reqListView.getItems().add(requestCell);
        });
    }
}
