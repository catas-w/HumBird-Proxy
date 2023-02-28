package com.catas.wicked.proxy;

import com.catas.wicked.proxy.proxy.ProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@ComponentScan
@Configuration
public class HttpProxyApplication {

    public static void main(String[] args) {
        log.info("Server starting...");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HttpProxyApplication.class);
        ProxyServer server = (ProxyServer) context.getBean("proxyServer");
        Object config = context.getBean("proxyConfig");
        server.start();
    }
}
