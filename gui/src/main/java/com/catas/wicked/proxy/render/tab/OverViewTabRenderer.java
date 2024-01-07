package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.OverviewInfo;
import com.catas.wicked.common.bean.PairEntry;
import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Singleton
public class OverViewTabRenderer extends AbstractTabRenderer {

    @Inject
    private DetailTabController detailTabController;

    @Inject
    private Cache<String, RequestMessage> requestCache;

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private OverviewInfo overviewInfo;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void render(RenderMessage renderMsg) {
        // System.out.println("-- render overview --");
        detailTabController.getOverViewMsgLabel().setVisible(renderMsg.isEmpty());
        if (renderMsg.isEmpty()) {
            return;
        }
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        displayOverView(request);
    }

    public void displayOverView(RequestMessage request) {
        TreeTableView<PairEntry> overviewTable = detailTabController.getOverviewTable();
        // if (overviewTable.getColumns() == null || overviewTable.getColumns().isEmpty()) {
        //     detailTabController.initTreeTable(overviewTable);
        // }
        String protocol = request.getProtocol() == null ? "-" : request.getProtocol();
        String url = request.getRequestUrl();
        String method = request.getMethod();
        if (method.contains("UNK")) {
            method = "-";
        }
        String title = String.format("%s %s %s", protocol, url, method);

        Map<String, String> map = new LinkedHashMap<>();
        ResponseMessage response = request.getResponse();
        String code = response == null ? "Waiting" : response.getStatusStr() + " " + response.getReasonPhrase();
        map.put("-- Request --", "");
        map.put("Url", url);
        map.put("Method", method);
        map.put("Status", code);
        map.put("Protocol", protocol);
        // map.put("Host", request.getRemoteHost());
        map.put("Remote Host", request.getRemoteHost());
        map.put("Remote Port", String.valueOf(request.getRemotePort()));
        map.put("Local Address", request.getLocalAddress());
        map.put("Local Port", String.valueOf(request.getLocalPort()));

        map.put("-- Timing --", "");
        map.put("Time Cost", response == null ? "-": response.getEndTime() - request.getStartTime() + "ms");
        map.put("Request Time", request.getStartTime() + "-" + request.getEndTime());
        map.put("Request Start", dateFormat.format(new Date(request.getStartTime())));
        map.put("Request End", dateFormat.format(new Date(request.getEndTime())));
        map.put("Response Time", response == null ? "-": response.getStartTime() + " - " + response.getEndTime());
        map.put("Response Start", response == null ? "-": dateFormat.format(new Date(response.getStartTime())));
        map.put("Response End", response == null ? "-": dateFormat.format(new Date(response.getEndTime())));

        map.put("-- Size --", "");
        map.put("Request Size", WebUtils.getHSize(request.getSize()));
        map.put("Response Size", response == null ? "-": WebUtils.getHSize(response.getSize()));
        map.put("Average Speed", getSpeed(request, response));

        // basic
        overviewInfo.getUrl().setVal(url);
        overviewInfo.getMethod().setVal(method);
        overviewInfo.getStatus().setVal(code);
        overviewInfo.getProtocol().setVal(protocol);
        overviewInfo.getRemoteHost().setVal(request.getRemoteHost());
        overviewInfo.getRemotePort().setVal(String.valueOf(request.getRemotePort()));
        overviewInfo.getClientHost().setVal(request.getLocalAddress());
        overviewInfo.getClientPort().setVal(String.valueOf(request.getLocalPort()));

        // timing
        overviewInfo.getTimeCost().setVal(response == null ? "-": response.getEndTime() - request.getStartTime() + "ms");
        overviewInfo.getRequestTime().setVal(request.getStartTime() + "-" + request.getEndTime());
        overviewInfo.getRequestStart().setVal(dateFormat.format(new Date(request.getStartTime())));
        overviewInfo.getRequestEnd().setVal(dateFormat.format(new Date(request.getEndTime())));
        overviewInfo.getRespTime().setVal(response == null ? "-": response.getStartTime() + " - " + response.getEndTime());
        overviewInfo.getRespStart().setVal(response == null ? "-": dateFormat.format(new Date(response.getStartTime())));
        overviewInfo.getRespEnd().setVal(response == null ? "-": dateFormat.format(new Date(response.getEndTime())));

        // size
        overviewInfo.getRequestSize().setVal(WebUtils.getHSize(request.getSize()));
        overviewInfo.getResponseSize().setVal(response == null ? "-": WebUtils.getHSize(response.getSize()));
        overviewInfo.getAverageSpeed().setVal(getSpeed(request, response));

        overviewTable.refresh();

        // String cont = title + "\n" + code + getContentStr(map);
        // renderHeaders(map, detailTabController.getOverviewTable());

        // Platform.runLater(() -> {
        //     detailTabController.getOverviewArea().replaceText(cont);
        // });
    }

    private String getSpeed(RequestMessage request, ResponseMessage response) {
        if (response == null || (request.getSize() == 0 && response.getSize() == 0)) {
            return "-";
        }
        int size = request.getSize() + response.getSize();
        int time = (int) (response.getEndTime() - request.getStartTime());
        return String.format("%.2f KB/s", (double) size/(double) time);
    }

    private String getContentStr(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        map.forEach((key, value) -> builder.append(key).append(": ").append(value).append("\n"));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
