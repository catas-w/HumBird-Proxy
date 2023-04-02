package com.catas.wicked.proxy.message;

import com.catas.wicked.proxy.bean.MessageEntity;
import com.catas.wicked.proxy.config.ApplicationConfig;
import com.catas.wicked.proxy.gui.componet.RequestCell;
import com.catas.wicked.proxy.gui.controller.RequestViewController;
import com.catas.wicked.proxy.util.ThreadPoolService;
import com.catas.wicked.proxy.util.WebUtils;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class MessageTree implements DisposableBean {

    private final TreeNode root = new TreeNode();

    private TreeNode latestNode;

    private boolean running;

    private Thread worker;

    @Autowired
    private ApplicationConfig appConfig;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private RequestViewController requestViewController;

    @PostConstruct
    public void init() {
        // fetch data from queue and add to message tree
        latestNode = root;
        running = true;
        worker = new Thread(() -> {
            while (running) {
                try {
                    MessageEntity msg = messageQueue.getMsg();
                    add(msg);
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

    @Override
    public void destroy() {
        running = false;
        this.worker.interrupt();
    }

    /**
     * 根据 path 添加节点到树中
     * @param msg request/response entity
     */
    private void add(MessageEntity msg) {
        // create leaf node
        TreeNode node = new TreeNode();
        node.setRequestId(msg.getRequestId());
        node.setMethod(msg.getMethod());
        node.setUrl(msg.getRequestUrl());
        node.setType(msg.getType());
        node.setLeaf(true);

        // add node to its position
        List<String> pathSplits = WebUtils.getPathSplits(msg.getUrl().toString());
        node.setPath(pathSplits.get(pathSplits.size() - 1));
        pathSplits.remove(pathSplits.size() - 1);

        TreeNode parent = findAndCreatParentNode(root, pathSplits, 0);
        parent.getRequestList().add(node);
        createTreeItemUI(parent, node);

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
        // TODO add event listener
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
        if (parent.isCreatedUI()) {
            createTreeItemUI(parent, node);
        }
        return findAndCreatParentNode(node, path, ++index);
    }
}
