package com.catas.wicked.common.bean;

import lombok.Data;

@Data
public class ReqSize {

    private int urlSize;
    private int headerSize;
    private int contentSize;

    public ReqSize(int urlSize, int headerSize, int contentSize) {
        this.urlSize = urlSize;
        this.headerSize = headerSize;
        this.contentSize = contentSize;
    }

    public ReqSize() {
    }

    public void reset() {
        urlSize = 0;
        headerSize = 0;
        contentSize = 0;
    }
}
