package com.catas.wicked.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.catas.wicked.server", "com.catas.wicked.common"})
public class HttpProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpProxyApplication.class, args);
    }
}
