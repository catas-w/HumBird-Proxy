package com.catas.wicked.proxy.message;

import com.catas.wicked.common.bean.RequestCell;
import com.catas.wicked.common.bean.message.BaseMessage;
import com.catas.wicked.common.bean.message.DeleteMessage;
import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.pipeline.Topic;
import com.catas.wicked.proxy.gui.componet.FilterableTreeItem;
import com.catas.wicked.proxy.gui.controller.ButtonBarController;
import com.catas.wicked.proxy.gui.controller.RequestViewController;
import com.catas.wicked.proxy.service.RequestViewService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.ehcache.spi.loaderwriter.BulkCacheWritingException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Singleton
public class MessageService {

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private RequestViewService requestViewService;

    @Inject
    private MessageQueue messageQueue;

    @Inject
    private Cache<String, RequestMessage> requestCache;

    @Inject
    private RequestViewController requestViewController;

    @Inject
    private ButtonBarController buttonBarController;

    private MessageTree messageTree;

    @Getter
    private final SimpleIntegerProperty requestCntProperty = new SimpleIntegerProperty(0);

    @PostConstruct
    public void init() {
        // TODO: use one thread-pool consumer
        messageQueue.subscribe(Topic.RECORD, this::processMsg);
        messageQueue.subscribe(Topic.UPDATE_MSG, this::processUpdate);
        // avoid circular dependency
        requestViewController.setMessageService(this);
        buttonBarController.setMessageService(this);
        resetMessageTree();
    }

    private void resetMessageTree() {
        messageTree = new MessageTree();
        messageTree.setRequestViewController(requestViewController);
        requestCntProperty.set(0);
    }

    private void refreshCntProperty() {
        if (messageTree.isEmpty()) {
            requestCntProperty.set(-1);
        } else {
            requestCntProperty.set(messageTree.getCount());
        }
    }

    /**
     * set selectionMode in treeView/listView
     * @param requestId requestId
     * @param fromTreeView source
     */
    public void selectRequestItem(String requestId, boolean fromTreeView) {
        if (requestId == null) {
            return;
        }

        RequestMessage requestMessage = requestCache.get(requestId);
        TreeNode treeNode = messageTree.findNodeByPath(requestMessage.getRequestUrl(), requestId);
        if (treeNode == null) {
            log.error("treeNode to select is null: {}", requestId);
            return;
        }
        if (fromTreeView) {
            requestViewController.getReqListView().getSelectionModel().select(treeNode.getListItem());
        } else {
            requestViewController.getReqTreeView().getSelectionModel().select(treeNode.getTreeItem());
        }
    }

    /**
     * update info for existed requestMsg/responseMsg
     * @param msg updateMsg
     */
    private void processUpdate(BaseMessage msg) {
        // TODO 更新 current request
        if (msg instanceof RequestMessage updateMsg) {
            RequestMessage requestMessage = requestCache.get(updateMsg.getRequestId());
            if (requestMessage == null) {
                // TODO: avoid
                System.out.println("requestMessage is null");
                return;
            }
            requestMessage.setSize(updateMsg.getSize());
            requestMessage.setEndTime(updateMsg.getEndTime());
            if (updateMsg.getClientStatus() != null) {
                requestMessage.setClientStatus(updateMsg.getClientStatus());
            }
            if (updateMsg.getBody() != null) {
                requestMessage.setBody(updateMsg.getBody());
            }
            if (updateMsg.getHeaders() != null) {
                requestMessage.getHeaders().putAll(updateMsg.getHeaders());
            }
            requestCache.put(requestMessage.getRequestId(), requestMessage);
        } else if (msg instanceof ResponseMessage updateMsg) {
            RequestMessage requestMessage = requestCache.get(updateMsg.getRequestId());
            if (requestMessage == null) {
                System.out.println("requestMessage is null");
                return;
            }
            if (requestMessage.getResponse() == null ) {
                if (updateMsg.getRetryTimes() > 0) {
                    updateMsg.setRetryTimes(updateMsg.getRetryTimes() - 1);
                    messageQueue.pushMsg(Topic.UPDATE_MSG, updateMsg);
                } else {
                    log.warn("Cannot update responseMsg, requestID = {}", requestMessage.getRequestId());
                }
                return;
            }
            // TODO 分开resp
            requestMessage.getResponse().setSize(updateMsg.getSize());
            requestMessage.getResponse().setEndTime(updateMsg.getEndTime());
            requestCache.put(requestMessage.getRequestId(), requestMessage);
        } else {
            log.warn("Unrecognized requestMsg");
        }
    }

    /**
     * record request and response msg
     * @param msg requestMessage/responseMessage
     */
    private void processMsg(BaseMessage msg) {
        if (msg instanceof RequestMessage requestMessage) {
            switch (requestMessage.getType()) {
                case REQUEST -> {
                    // put to cache
                    requestCache.put(requestMessage.getRequestId(), requestMessage);
                    messageTree.add(requestMessage);
                    refreshCntProperty();
                }
                case REQUEST_CONTENT -> {
                    // 添加请求体
                    RequestMessage contentMsg = (RequestMessage) msg;
                    RequestMessage data = requestCache.get(contentMsg.getRequestId());
                    if (data != null) {
                        data.setBody(contentMsg.getBody());
                        requestCache.put(data.getRequestId(), data);
                    }
                }
            }
        }

        if (msg instanceof ResponseMessage responseMessage) {
            switch (responseMessage.getType()) {
                case RESPONSE -> {
                    ResponseMessage respMessage = (ResponseMessage) msg;
                    RequestMessage data = requestCache.get(respMessage.getRequestId());
                    if (data != null) {
                        data.setResponse(respMessage);
                        requestCache.put(data.getRequestId(), data);
                    }
                }
                case RESPONSE_CONTENT -> {
                    // 添加响应体
                    // TODO 分开resp
                    ResponseMessage respMessage = (ResponseMessage) msg;
                    RequestMessage data = requestCache.get(respMessage.getRequestId());
                    if (data != null && data.getResponse() != null) {
                        data.getResponse().setContent(respMessage.getContent());
                        requestCache.put(data.getRequestId(), data);
                    }
                }
            }
        }

        if (msg instanceof DeleteMessage deleteMessage) {
            if (deleteMessage.isCleanLeaves()) {
                cleanLeaves();
            } else if (deleteMessage.isRemoveAll()){
                removeAll();
            } else {
                deleteRequest(deleteMessage);
            }
            refreshCntProperty();
        }
    }

    /**
     * delete request from gui and cache
     * @param deleteMessage deleteMessage
     */
    private void deleteRequest(DeleteMessage deleteMessage) {
        RequestCell requestCell = deleteMessage.getRequestCell();
        if (requestCell == null || StringUtils.isBlank(requestCell.getFullPath())) {
            return;
        }

        // TODO update requestDetailView
        // find node to delete
        String requestId = requestCell.isLeaf() ? requestCell.getRequestId() : null;
        TreeNode nodeToDelete = messageTree.findNodeByPath(requestCell.getFullPath(), requestId);
        if (nodeToDelete == null) {
            return;
        }
        log.info("Node to delete: {}", nodeToDelete.getFullPath());

        if (deleteMessage.getSource() == DeleteMessage.Source.LIST_VIEW) {
            // delete treeItem
            // 直接删除 treeView 中对应的叶子节点
            TreeItem<RequestCell> treeItemToDelete = nodeToDelete.getTreeItem();
            Platform.runLater(() -> {
                // treeItemToDelete.getParent().getChildren().remove(treeItemToDelete);
                FilterableTreeItem<RequestCell> parent = (FilterableTreeItem<RequestCell>) treeItemToDelete.getParent();
                parent.getInternalChildren().remove(treeItemToDelete);
            });
        }

        Set<String> requestIdList = new HashSet<>();
        List<RequestCell> listItemList = new ArrayList<>();
        messageTree.travel(nodeToDelete, treeNode -> {
            requestIdList.add(treeNode.getRequestId());
            listItemList.add(treeNode.getListItem());
            if (StringUtils.equals(appConfig.getObservableConfig().getCurrentRequestId(), treeNode.getRequestId())) {
                // System.out.println("***** remove reqId: " + treeNode.getRequestId());
                requestViewService.updateRequestTab(null);
            }
        });

        if (deleteMessage.getSource() == DeleteMessage.Source.TREE_VIEW) {
            // delete listItem
            // 若删除来自 treeView 需删除子结点中关联的 listItem
            ListView<RequestCell> reqListView = requestViewController.getReqListView();
            ObservableList<RequestCell> reqSourceList = requestViewController.getReqSourceList();
            Platform.runLater(() -> {
                // reqListView.getItems().removeAll(listItemList);
                reqSourceList.removeAll(listItemList);
            });
        }
        messageTree.delete(nodeToDelete);
        messageTree.subtractCnt(requestIdList.size());

        // remove requestId from ehcache
        try {
            requestCache.removeAll(requestIdList);
        } catch (BulkCacheWritingException e) {
            log.error("Error in deleting in cache.", e);
        }
    }

    /**
     * delete all leaf-nodes
     */
    private void cleanLeaves() {
        Set<String> requestIdList = new HashSet<>();
        List<TreeNode> treeNodeList = new ArrayList<>();

        // delete leafNodes in treeView
        messageTree.travelRoot(treeNode -> {
            requestIdList.add(treeNode.getRequestId());
            // delete current leaf-node
            FilterableTreeItem<RequestCell> nodeParent = treeNode.getParent().getTreeItem();
            treeNodeList.add(treeNode);
            Platform.runLater(() -> {
                nodeParent.getInternalChildren().remove(treeNode.getTreeItem());
            });
        });
        treeNodeList.forEach(messageTree::delete);
        messageTree.resetCnt();
        requestViewService.updateRequestTab(null);

        // delete all items in listView
        ObservableList<RequestCell> reqSourceList = requestViewController.getReqSourceList();
        Platform.runLater(() -> reqSourceList.remove(0, reqSourceList.size()));

        // delete in cache
        try {
            requestCache.removeAll(requestIdList);
        } catch (BulkCacheWritingException e) {
            log.error("Error in deleting in cache.", e);
        }
    }

    /**
     * remove all request data
     */
    private void removeAll() {
        Platform.runLater(() -> {
            requestViewController.getTreeRoot().getInternalChildren().clear();
            requestViewController.getReqSourceList().clear();
        });
        resetMessageTree();
        messageTree.resetCnt();
        requestViewService.updateRequestTab(null);

        try {
            requestCache.clear();
        } catch (Exception e) {
            log.error("Error in deleting in cache.", e);
        }
    }
}
