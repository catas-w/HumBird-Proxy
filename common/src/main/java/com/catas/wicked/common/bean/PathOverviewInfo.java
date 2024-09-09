package com.catas.wicked.common.bean;

import jakarta.inject.Singleton;
import lombok.Getter;

@Getter
@Singleton
public class PathOverviewInfo {

    private final PairEntry host = new PairEntry("Host");
    private final PairEntry port = new PairEntry("Port");
    private final PairEntry path = new PairEntry("Path");
    private final PairEntry protocol = new PairEntry("Protocol");
    private final PairEntry totalCnt = new PairEntry("Total");
    private final PairEntry getCnt = new PairEntry("GET");
    private final PairEntry postCnt = new PairEntry("POST");
    private final PairEntry putCnt = new PairEntry("PUT");
    private final PairEntry deleteCnt = new PairEntry("DELETE");
    private final PairEntry patchCnt = new PairEntry("PATCH");

    private final PairEntry timeCost = new PairEntry("Time Cost");
    private final PairEntry startTime = new PairEntry("Start");
    private final PairEntry endTime = new PairEntry("End");
    private final PairEntry averageSpeed = new PairEntry("Average Speed");

    private final PairEntry totalSize = new PairEntry("Total");
    private final PairEntry requestsSize = new PairEntry("Requests");
    private final PairEntry responsesSize = new PairEntry("Responses");

}
