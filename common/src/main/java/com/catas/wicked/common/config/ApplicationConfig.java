package com.catas.wicked.common.config;

import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.ThreadPoolService;
import com.catas.wicked.common.util.WebUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
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

    private AtomicBoolean shutDownFlag;

    private String host = "127.0.0.1";

    private Integer port = 9999;

    private Boolean handleSsl = true;

    private Boolean recording = true;

    private Boolean systemProxy = true;

    private Integer throttleLevel = 0;

    private Integer maxContentSize = 10 * 1024 * 1024;

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
    private ObjectMapper objectMapper;

    @Inject
    private MessageQueue messageQueue;

    @PostConstruct
    public void init() {
        this.currentRequestId = new AtomicReference<>(null);
        this.shutDownFlag = new AtomicBoolean(false);
        this.proxyLoopGroup = new NioEventLoopGroup(2);

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

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

    private File getLocalConfigFile() throws IOException {
        Path configPath = Paths.get(WebUtils.getStoragePath(), "config", "config.json");
        return configPath.toFile();
    }

    public void loadLocalConfig() throws IOException {
        File file = getLocalConfigFile();
        if (!file.exists()) {
            return;
        }

        // ApplicationConfig config = objectMapper.readValue(file, ApplicationConfig.class);
        JsonNode config = objectMapper.readValue(file, JsonNode.class);
        System.out.println("Read config: " + config);
        if (config == null) {
            return;
        }
        if (config.get("host") != null) {
            setHost(config.get("host").asText());
        }
        if (config.get("port") != null) {
            setPort(config.get("port").asInt());
        }
        if (config.get("throttleLevel") != null) {
            setThrottleLevel(config.get("throttleLevel").asInt());
        }
        if (config.get("maxContentSize") != null) {
            setMaxContentSize(config.get("maxContentSize").asInt());
        }
        if (config.get("handleSsl") != null) {
            setHandleSsl(config.get("handleSsl").asBoolean());
        }
        if (config.get("systemProxy") != null) {
            setSystemProxy(config.get("systemProxy").asBoolean());
        }
        if (config.get("externalProxy") != null) {
            JsonNode node = config.get("externalProxy");
            ExternalProxyConfig obj = objectMapper.treeToValue(node, ExternalProxyConfig.class);
            System.out.println(obj);
            setExternalProxy(obj);
        }
    }

    public synchronized void updateLocalConfig() {
        try {
            File file = getLocalConfigFile();
            ApplicationConfig config = new ApplicationConfig();
            config.setHost(getHost());
            config.setPort(getPort());
            config.setSystemProxy(isSystemProxy());
            config.setHandleSsl(isHandleSsl());
            config.setMaxContentSize(getMaxContentSize());
            config.setThrottleLevel(getThrottleLevel());
            config.setExternalProxy(getExternalProxy());
            objectMapper.writeValue(file, config);
        } catch (IOException e) {
            log.error("Error updating local config.", e);
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

    public Boolean isHandleSsl() {
        return handleSsl;
    }

    public Boolean isRecording() {
        return recording;
    }

    public Boolean isSystemProxy() {
        return systemProxy;
    }
}
