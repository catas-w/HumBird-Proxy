package com.catas.wicked.common.config;

import com.catas.wicked.common.bean.PoisonMessage;
import com.catas.wicked.common.constant.ProxyProtocol;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.AppContextUtil;
import com.catas.wicked.common.util.ThreadPoolService;
import io.netty.handler.ssl.SslContext;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Configuration
public class ApplicationConfig {

    private AtomicBoolean shutDownFlag;

    private String host = "127.0.0.1";

    private int port = 9999;

    private boolean handleSsl = true;

    private boolean recording = true;

    private int throttleLevel = 0;

    private int maxContentSize = 1 * 1024 * 1024;

    private ExternalProxyConfig externalProxyConfig;

    private SslContext clientSslCtx;
    private String issuer;
    private Date caNotBefore;
    private Date caNotAfter;
    private PrivateKey caPriKey;
    private PrivateKey serverPriKey;
    private PublicKey serverPubKey;

    @PostConstruct
    private void init() {
        this.shutDownFlag = new AtomicBoolean(false);
        this.externalProxyConfig = new ExternalProxyConfig();

        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        // test
        externalProxyConfig.setProtocol(ProxyProtocol.SOCKS4);
        externalProxyConfig.setProxyAddress("127.0.01", 10808);
        externalProxyConfig.setUsingExternalProxy(false);
    }

    public void shutDownApplication() {
        shutDownFlag.compareAndSet(false, true);
        MessageQueue messageQueue = AppContextUtil.getBean(MessageQueue.class);
        messageQueue.pushMsg(new PoisonMessage());
        ThreadPoolService.getInstance().shutdown();
    }
}
