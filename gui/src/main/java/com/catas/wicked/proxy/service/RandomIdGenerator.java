package com.catas.wicked.proxy.service;

import com.catas.wicked.common.bean.IdGenerator;
import com.catas.wicked.common.util.IdUtil;
import jakarta.inject.Singleton;

@Singleton
public class RandomIdGenerator implements IdGenerator {

    @Override
    public String nextId() {
        return IdUtil.getId();
    }
}
