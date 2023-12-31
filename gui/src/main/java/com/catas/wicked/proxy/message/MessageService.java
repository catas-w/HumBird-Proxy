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
import com.catas.wicked.proxy.gui.controller.RequestViewController;
import com.catas.wicked.proxy.service.RequestViewService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
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
    private MessageTree messageTree;

    @Inject
    private RequestViewController requestViewController;

    @PostConstruct
    public void init() {
        messageQueue.subscribe(Topic.RECORD, this::processMsg);
        messageQueue.subscribe(Topic.UPDATE_MSG, this::processUpdate);
    }

    private void processUpdate(BaseMessage msg) {
        if (msg instanceof RequestMessage updateMsg) {
            RequestMessage requestMessage = requestCache.get(updateMsg.getRequestId());
            if (requestMessage == null) {
                return;
            }
            requestMessage.setSize(updateMsg.getSize());
            requestMessage.setEndTime(updateMsg.getEndTime());
            requestCache.put(requestMessage.getRequestId(), requestMessage);
        } else if (msg instanceof ResponseMessage updateMsg) {
            RequestMessage requestMessage = requestCache.get(updateMsg.getRequestId());
            if (requestMessage == null) {
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

    private void processMsg(BaseMessage msg) {
        if (msg instanceof RequestMessage requestMessage) {
            switch (requestMessage.getType()) {
                case REQUEST -> {
                    // put to cache
                    requestCache.put(requestMessage.getRequestId(), requestMessage);
                    messageTree.add(requestMessage);
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
            deleteRequest(deleteMessage);
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
                treeItemToDelete.getParent().getChildren().remove(treeItemToDelete);
            });
        }

        Set<String> requestIdList = new HashSet<>();
        List<RequestCell> listItemList = new ArrayList<>();
        messageTree.travel(nodeToDelete, treeNode -> {
            requestIdList.add(treeNode.getRequestId());
            listItemList.add(treeNode.getListItem());
            if (StringUtils.equals(appConfig.getCurrentRequestId().get(), treeNode.getRequestId())) {
                System.out.println("***** remove reqId: " + treeNode.getRequestId());
                requestViewService.updateRequestTab(RenderMessage.EMPTY_MSG);
            }
        });

        if (deleteMessage.getSource() == DeleteMessage.Source.TREE_VIEW) {
            // delete listItem
            // 若删除来自 treeView 需删除子结点中关联的 listItem
            ListView<RequestCell> reqListView = requestViewController.getReqListView();
            Platform.runLater(() -> {
                reqListView.getItems().removeAll(listItemList);
            });
        }
        messageTree.delete(nodeToDelete);

        // remove requestId from ehcache
        // System.out.println("Travel result: " + requestIdList);
        try {
            requestCache.removeAll(requestIdList);
        } catch (BulkCacheWritingException e) {
            log.error("Error in deleting in cache.", e);
        }
    }
}
