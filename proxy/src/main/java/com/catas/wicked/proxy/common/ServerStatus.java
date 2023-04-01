package com.catas.wicked.proxy.common;

public enum ServerStatus {

    INIT("init"),
    RUNNING("running");

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    ServerStatus(String status) {
        this.status = status;
    }
}
