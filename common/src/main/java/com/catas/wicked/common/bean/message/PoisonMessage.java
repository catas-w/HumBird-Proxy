package com.catas.wicked.common.bean.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PoisonMessage extends BaseMessage{

    private MessageType type = MessageType.POISON;
}
