package com.catas.wicked.common.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PoisonMessage extends BaseMessage{

    private String type = "poison";
}
