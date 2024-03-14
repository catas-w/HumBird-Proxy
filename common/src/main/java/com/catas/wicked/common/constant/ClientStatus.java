package com.catas.wicked.common.constant;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientStatus implements Serializable {

    public enum Status {
        WAITING,
        FINISHED,
        CONNECT_ERR,
        REJECTED,
        TIMEOUT,
        ADDR_NOTFOUND,
        CLOSED,
        SSL_INVALID,
        UNKNOWN_ERR;
    }

    private Status status;
    private String msg;


    public ClientStatus() {
        status = Status.WAITING;
    }

    public boolean isFinished() {
        return this.status != Status.WAITING;
    }

    public boolean isSuccess() {
        return this.status == Status.FINISHED;
    }

    public ClientStatus copy() {
        ClientStatus copy = new ClientStatus();
        copy.setStatus(this.getStatus());
        copy.setMsg(this.getMsg());
        return copy;
    }
}
