package com.catas.wicked.common.constant;

import lombok.Getter;

@Getter
public enum ThrottlePreset {

    GPRS(50 * 1000, 20 * 1000),
    REGULAR_2G(250 * 1000, 50 * 1000),
    REGULAR_3G(750 * 1000, 250 * 1000),
    REGULAR_4G(4000 * 1000, 3000 * 1000);

    /**
     * 0 or a limit in bytes/s
     */
    private final long writeLimit;

    /**
     * 0 or a limit in bytes/s
     */
    private final long readLimit;

    /**
     * The delay between two computations of performances for channels
     */
    private final long checkInterval;

    /**
     * The maximum delay to wait in case of traffic excess.
     */
    private final long maxTime;

    ThrottlePreset(long writeLimit, long readLimit, long checkInterval, long maxTime) {
        this.writeLimit = writeLimit;
        this.readLimit = readLimit;
        this.checkInterval = checkInterval;
        this.maxTime = maxTime;
    }

    ThrottlePreset(long writeLimit, long readLimit) {
        this.writeLimit = writeLimit;
        this.readLimit = readLimit;
        this.checkInterval = 15000;
        this.maxTime = 1000;
    }
}
