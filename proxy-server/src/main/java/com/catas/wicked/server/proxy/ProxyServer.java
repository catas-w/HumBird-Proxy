package com.catas.wicked.server.proxy;

import com.catas.wicked.common.util.ThreadPoolService;
import com.catas.wicked.server.HttpProxyApplication;
import com.catas.wicked.server.cert.CertPool;
import com.catas.wicked.server.cert.CertService;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.server.handler.server.ProxyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;


@Slf4j
@Singleton
public class ProxyServer {

    @Inject
    private ApplicationConfig applicationConfig;

    @Inject
    private CertService certService;

    @Inject
    private CertPool certPool;

    @Inject
    private ProxyServerInitializer proxyServerInitializer;

    private ChannelFuture channelFuture;

    public ProxyServer() {
    }

    public void start() {
        log.info("--- Proxy server Starting ---");
        NioEventLoopGroup workGroup = new NioEventLoopGroup(2);
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();

        try {
            InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(proxyServerInitializer);
            channelFuture = bootstrap.bind(applicationConfig.getPort()).sync();
            // channelFuture.channel().closeFuture().sync();
            channelFuture.channel().closeFuture().addListener(future -> {
                System.out.println("** Close **");
                if (!(bossGroup.isShutdown() || bossGroup.isShuttingDown())) {
                    bossGroup.shutdownGracefully();
                }
                if (!(workGroup.isShutdown() || workGroup.isShuttingDown())) {
                    workGroup.shutdownGracefully();
                }
            });
        } catch (InterruptedException e) {
            log.info("Proxy server interrupt: {}", e.getMessage());
        } finally {
            // System.out.println("Server closed!!!");
            // EventLoopGroup proxyLoopGroup = applicationConfig.getProxyLoopGroup();
            // if (!(proxyLoopGroup.isShutdown() || proxyLoopGroup.isShuttingDown())) {
            //     proxyLoopGroup.shutdownGracefully();
            // }
            // if (!(bossGroup.isShutdown() || bossGroup.isShuttingDown())) {
            //     bossGroup.shutdownGracefully();
            // }
            // if (!(workGroup.isShutdown() || workGroup.isShuttingDown())) {
            //     workGroup.shutdownGracefully();
            // }

        }
    }

    public void shutdown() {
        log.info("--- Shutting down proxy server ---");
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
    }

    @PostConstruct
    private void init() {
        SslContextBuilder contextBuilder = SslContextBuilder.forClient()
                .sslProvider(SslProvider.OPENSSL)
                .startTls(true)
                .protocols("TLSv1.1", "TLSv1.2", "TLSv1")
                .trustManager(InsecureTrustManagerFactory.INSTANCE);
        X509Certificate caCert;
        PrivateKey caPriKey;
        try {
            applicationConfig.setClientSslCtx(contextBuilder.build());
            caCert = certService.loadCert(HttpProxyApplication.class.getResource("/cert/cert.crt").openStream());
            caPriKey = certService.loadPriKey(HttpProxyApplication.class.getResource("/cert/private.key").openStream());
            applicationConfig.setIssuer(certService.getSubject(caCert));
            applicationConfig.setCaNotBefore(caCert.getNotBefore());
            applicationConfig.setCaNotAfter(caCert.getNotAfter());
            //CA私钥用于给动态生成的网站SSL证书签证
            applicationConfig.setCaPriKey(caPriKey);
            //生产一对随机公私钥用于网站SSL证书动态创建
            KeyPair keyPair = certService.genKeyPair();
            applicationConfig.setServerPriKey(keyPair.getPrivate());
            applicationConfig.setServerPubKey(keyPair.getPublic());
        } catch (Exception e) {
            log.error("Certificate load error: ", e);
            applicationConfig.setHandleSsl(false);
        }

        ThreadPoolService.getInstance().run(() -> {
            try {
                start();
            } catch (Exception e) {
                log.error("Error in starting proxy server.", e);
                Platform.runLater(() -> {
                    alert("Port: " + applicationConfig.getPort() + " is unavailable, change port in settings");
                });
            }
        });
        // start();
    }

    private void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }
}
