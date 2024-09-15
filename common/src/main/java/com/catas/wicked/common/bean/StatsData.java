package com.catas.wicked.common.bean;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class StatsData {

    private long timestamp;

    private int count;

    private Map<HttpMethod, Integer> countMap;

    private long timeCost;

    private Date startTime;

    private Date endTime;

    private double averageSpeed;

    private long totalSize;

    private long requestsSize;

    private long responsesSize;

    public StatsData() {
        this.timestamp = System.currentTimeMillis();
    }

    public void addTimeCost(long time) {
        if (time > 0) {
            this.timeCost += time;
        }
    }

    public void addTotalSize(long size) {
        if (size > 0) {
            this.totalSize += size;
        }
    }

    public void addRequestsSize(long size) {
        if (size > 0) {
            this.requestsSize += size;
        }
    }

    public void addResponsesSize(long size) {
        if (size > 0) {
            this.responsesSize += size;
        }
    }
}
