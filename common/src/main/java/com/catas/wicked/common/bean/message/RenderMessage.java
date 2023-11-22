package com.catas.wicked.common.bean.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RenderMessage extends BaseMessage {

    private String requestId;

    private Tab tab;

    public RenderMessage() {
    }

    public RenderMessage(String requestId, Tab tab) {
        this.requestId = requestId;
        this.tab = tab;
    }

    public enum Tab {

        EMPTY(0),
        OVERVIEW(1),
        REQUEST(0),
        RESPONSE(2),
        TIMING(3),
        COOKIE(4);

        private final int order;

        Tab(int i) {
            this.order = i;
        }

        public int getOrder() {
            return order;
        }
    }
}
