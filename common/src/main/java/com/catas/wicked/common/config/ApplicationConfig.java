package com.catas.wicked.common.config;

import com.catas.wicked.common.constant.ProxyProtocol;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.ThreadPoolService;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Data
@Singleton
public class ApplicationConfig implements AutoCloseable {

    private AtomicBoolean shutDownFlag;

    private String host = "127.0.0.1";

    private int port = 9999;

    private boolean handleSsl = true;

    private boolean recording = true;

    private boolean setSystemProxy;

    private int throttleLevel = 0;

    private int maxContentSize = 1 * 1024 * 1024;

    private ExternalProxyConfig externalProxyConfig;

    private EventLoopGroup proxyLoopGroup;

    /**
     * ssl configs
     */
    private SslContext clientSslCtx;
    private String issuer;
    private Date caNotBefore;
    private Date caNotAfter;
    private PrivateKey caPriKey;
    private PrivateKey serverPriKey;
    private PublicKey serverPubKey;

    /**
     * current requestId in display
     */
    private AtomicReference<String> currentRequestId;

    @Inject
    private MessageQueue messageQueue;

    @PostConstruct
    public void init() {
        this.currentRequestId = new AtomicReference<>(null);
        this.shutDownFlag = new AtomicBoolean(false);
        this.externalProxyConfig = new ExternalProxyConfig();
        this.proxyLoopGroup = new NioEventLoopGroup(2);

        // test
        // System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        // externalProxyConfig.setProtocol(ProxyProtocol.SOCKS4);
        // externalProxyConfig.setProxyAddress("127.0.0.1", 10808);
        // externalProxyConfig.setUsingExternalProxy(true);
    }

    public void shutDownApplication() {
        shutDownFlag.compareAndSet(false, true);
        // MessageQueue messageQueue = AppContextUtil.getBean(MessageQueue.class);
        // messageQueue.pushMsg(new PoisonMessage());
        ThreadPoolService.getInstance().shutdown();
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        shutDownApplication();
    }
}
