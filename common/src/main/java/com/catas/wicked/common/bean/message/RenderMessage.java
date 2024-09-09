package com.catas.wicked.common.bean.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class RenderMessage extends BaseMessage {

    public final static String EMPTY_MSG = "_EMPTY_";

    public final static String PATH_MSG = "PATH_";

    private String requestId;

    private Tab targetTab;

    private boolean isEmpty;

    private boolean isPath;

    public RenderMessage() {
    }

    public RenderMessage(String requestId, Tab tab) {
        this.requestId = requestId;
        this.targetTab = tab;
        this.isEmpty = StringUtils.equals(requestId, EMPTY_MSG);
        this.isPath = requestId.startsWith(PATH_MSG);
    }

    @Getter
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

        public static Tab valueOfIgnoreCase(String value) {
            if (value == null) {
                return null;
            }

            String strip = value.strip();
            for (Tab tab : Tab.values()) {
                if (StringUtils.equalsIgnoreCase(tab.name(), strip)) {
                    return tab;
                }
            }
            return null;
        }
    }
}
