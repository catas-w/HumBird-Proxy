package com.catas.wicked.proxy.message;

import com.catas.wicked.proxy.gui.componet.RequestCell;
import io.netty.handler.codec.http.HttpMethod;
import javafx.scene.control.TreeItem;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
class TreeNode {
    /**
     * request/response
     */
    private String type;
    /**
     * request id
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
    private TreeItem<RequestCell> treeItem;
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
