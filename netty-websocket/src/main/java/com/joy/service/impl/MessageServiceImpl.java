package com.joy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.joy.entity.ChatMsg;
import com.joy.entity.MessageObject;
import com.joy.service.MessageService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Joy
 */
@Service
public class MessageServiceImpl implements MessageService {

    private final AttributeKey<String> userKey = AttributeKey.valueOf("user");

    private final AttributeKey<String> groupKey = AttributeKey.valueOf("group");

    private static final Map<String, ChannelGroup> GROUPS = new ConcurrentHashMap<>();

    @Override

    public void putConnection(String userId, String groupId, Channel channel) {
        channel.attr(userKey).set(userId);
        channel.attr(groupKey).set(groupId);
        ChannelGroup channelGroup = GROUPS.get(groupId);
        if (null == channelGroup) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            channelGroup.add(channel);
            GROUPS.put(groupId, channelGroup);
        } else {
            channelGroup.add(channel);
        }
    }

    @Override
    public void removeConnection(NioSocketChannel channel) {
        Iterator<Map.Entry<String, ChannelGroup>> iterator = GROUPS.entrySet().iterator();
        while (iterator.hasNext()) {
            ChannelGroup channelGroup = iterator.next().getValue();
            channelGroup.remove(channel);
            if (channelGroup.size() == 0) {
                iterator.remove();
            }
        }

    }

    @Override
    public void sendMessage(MessageObject messageObject, ChannelHandlerContext ctx) {
        ChatMsg chatMsg = JSON.parseObject(messageObject.getData().toString(), ChatMsg.class);
        ChannelGroup channelGroup = GROUPS.get(chatMsg.getGroupId());
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = inetSocketAddress.getAddress().getHostAddress();
        if (channelGroup == null || channelGroup.size() == 0) {
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(messageObject)));
    }
}