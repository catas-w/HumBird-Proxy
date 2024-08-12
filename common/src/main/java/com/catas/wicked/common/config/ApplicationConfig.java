package com.catas.wicked.common.config;

import com.catas.wicked.common.constant.ServerStatus;
import com.catas.wicked.common.constant.SystemProxyStatus;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.executor.ThreadPoolService;
import com.catas.wicked.common.util.WebUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@Data
@Singleton
public class ApplicationConfig implements AutoCloseable {

    private String host = "127.0.0.1";

    private Integer defaultThreadNumber = 2;

    private EventLoopGroup proxyLoopGroup;

    private Settings settings;

    private String settingPath;

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
     * status of proxy server
     * TODO: bind gui style
     */
    private ServerStatus serverStatus;
    private SystemProxyStatus systemProxyStatus;

    /**
     * current requestId in display
     */
    private final AtomicReference<String> currentRequestId = new AtomicReference<>(null);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean shutDownFlag = new AtomicBoolean(false);

    @Inject
    private MessageQueue messageQueue;

    @PostConstruct
    public void init() {
        // this.currentRequestId = new AtomicReference<>(null);
        // this.shutDownFlag = new AtomicBoolean(false);
        this.proxyLoopGroup = new NioEventLoopGroup(defaultThreadNumber);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            loadSettings();
        } catch (IOException e) {
            log.error("Error loading local configuration.", e);
        }

        // Runtime.getRuntime().addShutdownHook(new Thread(){
        //     @Override
        //     public void run() {
        //         close();
        //     }
        // });
        // test
        // System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        // externalProxyConfig.setProtocol(ProxyProtocol.SOCKS4);
        // externalProxyConfig.setProxyAddress("127.0.0.1", 10808);
        // externalProxyConfig.setUsingExternalProxy(true);
    }

    private File getLocalConfigFile() throws IOException {
        Path configPath;
        if (StringUtils.isBlank(settingPath)) {
            configPath = Paths.get(WebUtils.getStoragePath(), "config", "config.json");
        } else {
            configPath = Paths.get(settingPath);
        }
        return configPath.toFile();
    }

    public void loadSettings() throws IOException {
        File file = getLocalConfigFile();
        if (!file.exists()) {
            log.info("Settings file not exist.");
            settings = new Settings();
            return;
        }

        settings = objectMapper.readValue(file, Settings.class);
    }

    public synchronized void updateSettings() {
        try {
            File file = getLocalConfigFile();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            objectMapper.writeValue(file, settings);
        } catch (IOException e) {
            // TODO alert
            log.error("Error updating local config.", e);
        }
    }

    public int getMaxContentSize() {
        if (settings == null || settings.getMaxContentSize() <= 0) {
            return 1024 * 1024;
        }
        return settings.getMaxContentSize() * 1024 * 1024;
    }

    public void shutDownApplication() {
        shutDownFlag.compareAndSet(false, true);
        if (!(proxyLoopGroup.isShutdown() || proxyLoopGroup.isShuttingDown())) {
            proxyLoopGroup.shutdownGracefully();
        }
        // MessageQueue messageQueue = AppContextUtil.getBean(MessageQueue.class);
        // messageQueue.pushMsg(new PoisonMessage());
        ThreadPoolService.getInstance().shutdown();
    }

    @PreDestroy
    @Override
    public void close() {
        shutDownApplication();
    }

}
