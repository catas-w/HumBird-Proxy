package com.catas.wicked.server.client;

import com.catas.wicked.common.config.ExternalProxyConfig;
import com.catas.wicked.common.util.ProxyHandlerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.resolver.NoopAddressResolverGroup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.catas.wicked.common.constant.NettyConstant.CLIENT_PROCESSOR;
import static com.catas.wicked.common.constant.NettyConstant.EXTERNAL_PROXY;
import static com.catas.wicked.common.constant.NettyConstant.HTTP_CODEC;
import static com.catas.wicked.common.constant.NettyConstant.SSL_HANDLER;

@Data
@Slf4j
public class MinimalHttpClient {

    private String uri;
    private HttpMethod method;
    private Map<String, String> headers;
    private byte[] content;
    private NioEventLoopGroup eventExecutors;
    private ExternalProxyConfig proxyConfig;
    private int timeout = 60 * 1000;
    private HttpVersion httpVersion = HttpVersion.HTTP_1_1;

    // private HttpRequest httpRequest;
    // private HttpContent httpContent;

    private HttpResponse httpResponse;
    private Object notifier = new Object();
    private ChannelFuture channelFuture;

    public void execute() {
        assert uri != null;
        if (eventExecutors == null) {
            eventExecutors = new NioEventLoopGroup();
        }
        Bootstrap bootstrap = new Bootstrap();

        InetSocketAddress address = null;
        boolean isSSl = uri.startsWith("https://");
        try {
            URL url = new URL(uri);
            String host = url.getHost();
            InetAddress addr = InetAddress.getByName(host);
            if (!host.equalsIgnoreCase(addr.getHostAddress())) {
                address = new InetSocketAddress(host, isSSl ? 443 : 80);
            } else {
                int port = url.getPort();
                address = InetSocketAddress.createUnresolved(host, port);
            }
        } catch (Exception e) {
            log.error("Illegal uri: {}", uri, e);
            throw new RuntimeException("Illegal Url: " + uri);
        }

        MinimalHttpClient client = this;
        bootstrap.group(eventExecutors)
                .remoteAddress(address)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        if (proxyConfig != null) {
                            // add external proxy handler
                            ProxyHandler httpProxyHandler = ProxyHandlerFactory.getExternalProxyHandler(proxyConfig);
                            ch.pipeline().addLast(EXTERNAL_PROXY, httpProxyHandler);
                            bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
                        } else {
                            bootstrap.resolver(DefaultAddressResolverGroup.INSTANCE);
                        }
                        if (isSSl) {
                            SslContext context = SslContextBuilder.forClient()
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                            ch.pipeline().addLast(SSL_HANDLER, context.newHandler(ch.alloc()));
                        }
                        ch.pipeline().addLast(HTTP_CODEC, new HttpClientCodec());
                        ch.pipeline().addLast(CLIENT_PROCESSOR, new MinimalClientHandler(client));
                    }
                });

        // ChannelFuture channelFuture = bootstrap.connect().sync();
        // channelFuture.channel().closeFuture().sync();

        channelFuture = bootstrap.connect();
        HttpRequest httpRequest = buildHttpRequest();
        List<HttpContent> httpContents = buildHttpContent();
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                channelFuture.channel().write(httpRequest);
                for (HttpContent httpContent : httpContents) {
                    channelFuture.channel().write(httpContent);
                }
                channelFuture.channel().flush();
            } else {
                log.error("Error in minimal httpClient.", future.cause());
                channelFuture.channel().close();
                throw new RuntimeException(future.cause());
            }
        });
    }

    public void close() {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        eventExecutors.shutdownGracefully();
    }

    public HttpResponse response() {
        // TODO wait to get response
        return httpResponse;
    }

    private HttpRequest buildHttpRequest() {
        DefaultHttpRequest request = new DefaultHttpRequest(httpVersion, method, uri);
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, value) -> request.headers().set(key, value));
        }
        return request;
    }

    private List<HttpContent> buildHttpContent() {
        List<HttpContent> list = new ArrayList<>();
        if (content != null) {
            DefaultHttpContent defaultHttpContent = new DefaultHttpContent(Unpooled.wrappedBuffer(content));
            list.add(defaultHttpContent);
        }
        list.add(new DefaultLastHttpContent());
        return list;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MinimalHttpClient httpClient;

        public Builder() {
            httpClient = new MinimalHttpClient();
        }

        public MinimalHttpClient build() {
            return httpClient;
        }

        public Builder uri(String uri) {
            httpClient.setUri(uri);
            return this;
        }

        public Builder method(HttpMethod method) {
            httpClient.setMethod(method);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            httpClient.setHeaders(headers);
            return this;
        }

        public Builder content(byte[] content) {
            httpClient.setContent(content);
            return this;
        }

        public Builder eventExecutors(NioEventLoopGroup eventExecutors) {
            httpClient.setEventExecutors(eventExecutors);
            return this;
        }

        public Builder proxyConfig(ExternalProxyConfig proxyConfig) {
            httpClient.setProxyConfig(proxyConfig);
            return this;
        }

        public Builder timeout(int timeout) {
            httpClient.setTimeout(timeout);
            return this;
        }

        public Builder httpVersion(String httpVersion) {
            httpClient.setHttpVersion(HttpVersion.valueOf(httpVersion));
            return this;
        }
    }
}
