package com.catas.wicked.common.constant;

public enum SystemProxyStatus {

    /**
     * server is not running, this status is meaningless
     */
    DISABLED,

    /**
     * turned off
     */
    OFF,

    /**
     * turned on, not consistent with system proxy
     */
    SUSPENDED,

    /**
     * turned on, consistent with system proxy
     */
    ON
}
