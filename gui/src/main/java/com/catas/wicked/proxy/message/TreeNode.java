package com.catas.wicked.proxy.message;

import com.catas.wicked.common.bean.RequestCell;
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
     * request id
     */
    private String requestId;
    /**
     * request method
     */
    private HttpMethod method;
    /**
     * full url
     */
    private String url;
    /**
     * separate path
     * eg. http://google.com, host, page, 1
     */
    private String path;
    /**
     * full path
     * eg. http://google.com, http://google.com/host/page
     */
    private String fullPath;

    private boolean isLeaf;

    /**
     * related tree item
     */
    private TreeItem<RequestCell> treeItem;
    /**
     * children: path-nodes
     */
    private Map<String, TreeNode> pathChildren;
    /**
     * children: leaf-nodes
     */
    private List<TreeNode> leafChildren;
    private TreeNode next;
    private TreeNode prev;

    TreeNode() {
        this.pathChildren = new HashMap<>();
        this.leafChildren = new LinkedList<>();
        this.url = "";
        this.path = "";
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "requestId='" + requestId + '\'' +
                ", method=" + method +
                ", path='" + path + '\'' +
                '}';
    }
}
