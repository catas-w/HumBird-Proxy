package com.catas.wicked.common.config;

import com.catas.wicked.common.executor.ScheduledThreadPoolService;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.executor.ThreadPoolService;
import com.catas.wicked.common.util.AlertUtils;
import com.catas.wicked.common.util.SystemUtils;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@Data
@Singleton
public class ApplicationConfig implements AutoCloseable {

    private String host = "127.0.0.1";

    private Integer defaultThreadNumber = 2;

    private EventLoopGroup proxyLoopGroup;

    private Settings settings;

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

    private final ObservableConfig observableConfig = new ObservableConfig();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean shutDownFlag = new AtomicBoolean(false);

    @Inject
    private MessageQueue messageQueue;

    @PostConstruct
    public void init() {
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
        Path configPath = SystemUtils.getStoragePath("config.json");
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

    public void updateSettings() {
        try {
            File file = getLocalConfigFile();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            objectMapper.writeValue(file, settings);
        } catch (IOException e) {
            log.error("Error updating local config.", e);
            AlertUtils.alertWarning("Error in updating settings.");
        }
    }

    public void updateSettingsAsync() {
        ThreadPoolService.getInstance().run(this::updateSettings);
    }

    public int getMaxContentSize() {
        if (settings == null || settings.getMaxContentSize() <= 0) {
            return 1024 * 1024;
        }
        return settings.getMaxContentSize() * 1024 * 1024;
    }

    /**
     * set root certificate
     * @param issuer issuer
     * @param caCert X509Certificate
     * @param caPriKey privateKey
     */
    public void updateRootCertConfigs(String issuer, X509Certificate caCert, PrivateKey caPriKey) {
        this.issuer = issuer;
        this.caNotBefore = caCert.getNotBefore();
        this.caNotAfter = caCert.getNotAfter();
        this.caPriKey = caPriKey;
    }

    public void shutDownApplication() {
        shutDownFlag.compareAndSet(false, true);
        if (!(proxyLoopGroup.isShutdown() || proxyLoopGroup.isShuttingDown())) {
            proxyLoopGroup.shutdownGracefully();
        }

        messageQueue.shutdown();
        ThreadPoolService.getInstance().shutdown();
        ScheduledThreadPoolService.getInstance().shutdown();
    }

    @PreDestroy
    @Override
    public void close() {
        shutDownApplication();
    }

}
