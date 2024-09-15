package com.catas.wicked.common.bean.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StatisticMessage extends BaseMessage {

    /**
     * full path
     */
    private String path;
}
