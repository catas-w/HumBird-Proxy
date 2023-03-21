package com.catas.wicked.proxy.config;

import com.catas.wicked.proxy.common.ProxyType;
import io.netty.handler.ssl.SslContext;
import lombok.Data;
import org.springframework.context.annotation.Configuration;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Data
@Configuration
public class ApplicationConfig {

    private String host = "127.0.0.1";

    private int port = 9999;

    private ProxyType proxyType = ProxyType.HTTP;

    private boolean handleSsl = true;

    private boolean recording = true;

    private int throttleLevel = 0;

    private int maxRequestSize = 1000;

    private SslContext clientSslCtx;
    private String issuer;
    private Date caNotBefore;
    private Date caNotAfter;
    private PrivateKey caPriKey;
    private PrivateKey serverPriKey;
    private PublicKey serverPubKey;
}
