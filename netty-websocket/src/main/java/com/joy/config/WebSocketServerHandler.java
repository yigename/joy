package com.joy.config;


import com.alibaba.fastjson.JSON;
import com.joy.entity.MessageObject;
import com.joy.service.MessageService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author Joy
 */
@Component
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    @Value("${netty.useSsl:false}")
    private boolean useSsl;

    @Resource
    private MessageService messageService;


    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            this.handlerHttpRequest(ctx, (HttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            WebSocketFrame wsf = (WebSocketFrame) msg;
            wsf.retain();
            this.handlerWebSocketFrame(ctx, wsf);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handlerHttpRequest(ChannelHandlerContext ctx, HttpRequest req) {
        String userId;
        String groupId;
        if ("GET".equalsIgnoreCase(req.getMethod().toString())) {
            String uri = req.uri();
            userId = uri.substring(uri.indexOf("/", 2) + 1, uri.lastIndexOf("/"));
            groupId = uri.substring(uri.lastIndexOf("/") + 1);
            NioSocketChannel channel = (NioSocketChannel) ctx.channel();
            messageService.putConnection(userId, groupId, channel);
        }
        //HTTP解码失败
        if (!req.decoderResult().isSuccess() || (!"websocket".equalsIgnoreCase(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, (FullHttpRequest) req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
        }
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(useSsl ? "wss://" : "ws://" +
                req.headers().get("Host") + "/" + req.uri(), null, false);
        if (null == (handshaker = factory.newHandshaker(req))) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        if (res.status().code() != 200) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(res.status().toString(), StandardCharsets.UTF_8);
            res.content().writeBytes(byteBuf);
        }
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame wsf) {
        if (wsf instanceof CloseWebSocketFrame) {
            NioSocketChannel channel = (NioSocketChannel) ctx.channel();
            messageService.removeConnection(channel);
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) wsf);
            return;
        }
        if (wsf instanceof PingWebSocketFrame) {
            log.info("ping");
            return;
        }
        if (wsf instanceof PongWebSocketFrame) {
            log.info("pong");
            return;
        }
        MessageObject messageObject = JSON.parseObject(((TextWebSocketFrame) wsf).text(), MessageObject.class);
        messageService.sendMessage(messageObject, ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            PingWebSocketFrame ping = new PingWebSocketFrame();
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                case WRITER_IDLE:
                    ctx.writeAndFlush(ping);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        messageService.removeConnection((NioSocketChannel) ctx.channel());
        ctx.close();
    }
}