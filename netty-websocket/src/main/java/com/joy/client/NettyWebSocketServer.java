package com.joy.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.TimeUnit;

/**
 * @author Joy
 */
@Component
@Slf4j
public class NettyWebSocketServer {

    private final EventLoopGroup boosGroup = new NioEventLoopGroup();

    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    private Channel channel;

    /**
     * start netty server
     *
     * @param port
     */
    public void start(int port) {
        log.info("netty server starting...");
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(boosGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("http-codec", new HttpServerCodec())
                                    .addLast("aggregator", new HttpObjectAggregator(65536))
                                    .addLast("http-chunked", new ChunkedWriteHandler())
                                    .addLast(new IdleStateHandler(60, 30, 60 * 30, TimeUnit.SECONDS));
                        }
                    });
            channel = server.bind(port).sync().channel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("netty server start success on port:{}", port);
    }

    /**
     * netty server destroys
     */
    @PreDestroy
    public void destroy() {
        log.info("netty server destroying");
        if (null != channel) {
            channel.close();
        }
        boosGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}