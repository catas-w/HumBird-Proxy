package com.catas.wicked.proxy.config;

import com.catas.wicked.proxy.cert.CertService;
import com.catas.wicked.proxy.common.ProxyType;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;

@Data
@Configuration
public class ProxyConfig {

    private String host = "127.0.0.1";

    private int port = 9999;

    private ProxyType proxyType = ProxyType.HTTP;

    private boolean handleSsl = true;

    private boolean recording = true;

    private int throttleLevel = 0;

    private SslContext clientSslCtx;
    private String issuer;
    private Date caNotBefore;
    private Date caNotAfter;
    private PrivateKey caPriKey;
    private PrivateKey serverPriKey;
    private PublicKey serverPubKey;
}
