package io.yue.im.server.application.netty.ws.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.yue.im.common.domain.model.IMSendInfo;

import java.util.List;

/**
 * websocket编码类
 */
public class WebSocketMessageProtocolEncoder extends MessageToMessageEncoder<IMSendInfo> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IMSendInfo imSendInfo, List<Object> list) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // 对象序列化为JSON
        String text = objectMapper.writeValueAsString(imSendInfo);
        // 创建WebSocket文本帧
        TextWebSocketFrame frame = new TextWebSocketFrame(text);
        // 添加到处理链
        list.add(frame);
    }
}
