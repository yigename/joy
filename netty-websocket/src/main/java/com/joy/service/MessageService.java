package com.joy.service;

import com.joy.entity.MessageObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author Joy
 */
public interface MessageService {

    void putConnection(String userId, String groupId, Channel channel);

    void removeConnection(NioSocketChannel channel);

    void sendMessage(MessageObject messageObject, ChannelHandlerContext ctx);
}