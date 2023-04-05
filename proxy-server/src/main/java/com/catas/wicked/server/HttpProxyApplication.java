package com.catas.wicked.server;

import com.catas.wicked.server.proxy.ProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.catas.wicked.server", "com.catas.wicked.common"})
public class HttpProxyApplication {

    public static void main(String[] args) {
        log.info("Server starting...");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HttpProxyApplication.class);
        ProxyServer server = (ProxyServer) context.getBean("proxyServer");
        Object config = context.getBean("applicationConfig");
        server.start();
    }
}
