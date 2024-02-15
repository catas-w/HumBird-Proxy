package com.catas.wicked.common.config;

import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.ThreadPoolService;
import com.catas.wicked.common.util.WebUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Data
@Singleton
public class ApplicationConfig implements AutoCloseable {

    private AtomicBoolean shutDownFlag;

    private String host = "127.0.0.1";

    private Integer port = 9999;

    private Boolean handleSsl = true;

    private Boolean recording = true;

    private Boolean systemProxy = true;

    private Integer throttleLevel = 0;

    private Integer maxContentSize = 1 * 1024 * 1024;

    private ExternalProxyConfig externalProxy;

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
        // this.externalProxyConfig = new ExternalProxyConfig();
        this.proxyLoopGroup = new NioEventLoopGroup(2);

        try {
            loadLocalConfig();
        } catch (IOException e) {
            log.error("Error loading local configuration.", e);
        }

        // test
        // System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        // externalProxyConfig.setProtocol(ProxyProtocol.SOCKS4);
        // externalProxyConfig.setProxyAddress("127.0.0.1", 10808);
        // externalProxyConfig.setUsingExternalProxy(true);
    }

    private void loadLocalConfig() throws IOException {
        Path configPath = Paths.get(WebUtils.getStoragePath(), "config", "config.json");
        File file = configPath.toFile();
        if (!file.exists()) {
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ApplicationConfig config = objectMapper.readValue(file, ApplicationConfig.class);
        // System.out.println("Read config: " + config);
        if (config == null) {
            return;
        }
        if (StringUtils.isNotBlank(config.getHost())) {
            setHost(config.getHost());
        }
        if (config.getPort() != null) {
            setPort(config.getPort());
        }
        if (config.getHandleSsl() != null) {
            setHandleSsl(config.getHandleSsl());
        }
        if (config.getMaxContentSize() != null) {
            setMaxContentSize(config.getMaxContentSize());
        }
        if (config.getSystemProxy() != null) {
            setSystemProxy(config.getSystemProxy());
        }
        if (config.getThrottleLevel() != null) {
            setThrottleLevel(config.getThrottleLevel());
        }
        if (config.getExternalProxy() != null) {
            setExternalProxy(config.getExternalProxy());
        }
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
