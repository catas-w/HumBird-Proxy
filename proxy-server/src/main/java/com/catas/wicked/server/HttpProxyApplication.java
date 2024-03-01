package com.catas.wicked.server;

import io.micronaut.context.BeanContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxyApplication {

    public static boolean startFromServer;

    public static void main(String[] args) {
        // Micronaut.run(HttpProxyApplication.class, args);
        startFromServer = true;
        BeanContext context = BeanContext.build();
        context.start();
    }
}
