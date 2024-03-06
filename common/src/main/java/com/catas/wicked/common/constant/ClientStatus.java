package com.catas.wicked.common.constant;

public enum ClientStatus {

    WAITING,
    FINISHED,
    REJECTED,
    TIMEOUT,
    ADDR_NOTFOUND,
    SSL_INVALID,
    UNKNOWN_ERR;

    public boolean isFinished() {
        return this != WAITING;
    }

    public boolean isSuccess() {
        return this == FINISHED;
    }
}
