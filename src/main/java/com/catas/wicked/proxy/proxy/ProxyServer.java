package com.catas.wicked.proxy.proxy;

import com.catas.wicked.proxy.cert.CertPool;
import com.catas.wicked.proxy.cert.CertService;
import com.catas.wicked.proxy.config.ProxyConfig;
import com.catas.wicked.proxy.proxy.handler.ProxyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ProxyServer {

    @Autowired
    private ProxyConfig proxyConfig;
    @Autowired
    private CertService certService;
    @Autowired
    private CertPool certPool;

    public ProxyServer() {
    }

    public void start() {
        NioEventLoopGroup workGroup = new NioEventLoopGroup(2);
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast("httpCodec", new HttpServerCodec());
                            channel.pipeline().addLast("serverHandle", new ProxyServerHandler(
                                    proxyConfig, certService, certPool
                            ));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(proxyConfig.getPort()).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error occured in proxy server: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
