package com.catas.wicked.common.bean;

import jakarta.inject.Singleton;
import lombok.Getter;

@Getter
@Singleton
public class RequestOverviewInfo {

    private final PairEntry url = new PairEntry("Url");
    private final PairEntry method = new PairEntry("Method");
    private final PairEntry status = new PairEntry("Status");
    private final PairEntry protocol = new PairEntry("Protocol");
    private final PairEntry remoteHost = new PairEntry("Remote Host");
    private final PairEntry remotePort = new PairEntry("Remote Port");
    private final PairEntry clientHost = new PairEntry("Client Host");
    private final PairEntry clientPort = new PairEntry("Client Port");

    private final PairEntry timeCost = new PairEntry("Time cost");
    private final PairEntry requestTime = new PairEntry("Request time");
    private final PairEntry requestStart = new PairEntry("Request start");
    private final PairEntry requestEnd = new PairEntry("Request end");
    private final PairEntry respTime = new PairEntry("Response time");
    private final PairEntry respStart = new PairEntry("Response start");
    private final PairEntry respEnd = new PairEntry("Response end");

    private final PairEntry requestSize = new PairEntry("Request size");
    private final PairEntry responseSize = new PairEntry("Response size");
    private final PairEntry averageSpeed = new PairEntry("Average speed");
}
