package com.catas.wicked.common.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseMessage implements Serializable {

    private String type;
}
