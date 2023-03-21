package com.catas.wicked.proxy.message;

import com.catas.wicked.proxy.bean.MessageEntity;
import com.catas.wicked.proxy.config.ApplicationConfig;
import com.catas.wicked.proxy.gui.controller.RequestViewController;
import com.catas.wicked.proxy.util.ThreadPoolService;
import io.netty.handler.codec.http.HttpMethod;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        node.requestId = msg.getRequestId();
        node.method = msg.getMethod();
        node.url = msg.getRequestUrl();
        node.type = msg.getType();
        node.isLeaf = true;

        // add node to its position
        ArrayList<String> pathSplits = new ArrayList<>();
        pathSplits.add(msg.getHost());
        pathSplits.addAll(Arrays.asList(msg.getPath().split("/")));
        TreeNode parent = findAndCreatParentNode(root, pathSplits, 0);
        parent.requestList.add(node);
        if (parent.isCreatedUI) {
            createTreeItemUI(parent, node);
        }

        latestNode.next = node;
        node.prev = latestNode;
        latestNode = node;
    }

    /**
     * 创建 tree item
     * @param parent parent node
     * @param node tree node
     */
    private void createTreeItemUI(TreeNode parent, TreeNode node) {
        // TODO add event listener
        if (parent == root) {
            parent.treeItem = requestViewController.getRoot();
        }
        TreeItem<String> parentTreeItem = parent.treeItem;
        TreeItem<String> treeItem = new TreeItem<>();
        treeItem.setValue(node.path);
        if (node.path.startsWith("http")) {
            FontIcon icon = new FontIcon();
            icon.setIconColor(Color.valueOf("#616161"));
            icon.setIconLiteral("fas-globe-africa");
            icon.setIconSize(12);
            treeItem.setGraphic(icon);
        }
        node.treeItem = treeItem;
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
        TreeNode node = parent.children.get(curPath);
        if (node == null) {
            node = new TreeNode();
            node.path = curPath;
            // node.url = parent.url + "/" + curPath;
            parent.children.put(curPath, node);
        }
        if (parent.isCreatedUI) {
            createTreeItemUI(parent, node);
        }
        return findAndCreatParentNode(node, path, ++index);
    }

    class TreeNode {
        /**
         * request/response
         */
        private String type;
        /**
         *  request id
         */
        private String requestId;
        /**
         * request method
         */
        private HttpMethod method;
        /**
         * host
         */
        private String host;
        /**
         * full url
         */
        private String url;
        /**
         * separate path
         */
        private String path;
        /**
         * data
         */
        private byte[] body;

        private boolean isLeaf;
        private boolean isCreatedUI = true;
        private TreeItem<String> treeItem;
        private Map<String, TreeNode> children;
        private List<TreeNode> requestList;
        private TreeNode next;
        private TreeNode prev;

        TreeNode() {
            this.children = new HashMap<>();
            this.requestList = new LinkedList<>();
            this.url = "";
            this.path = "";
        }
    }
}
