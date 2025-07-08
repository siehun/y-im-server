package io.yue.im.server.application.netty.tcp.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.model.IMSendInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * tcp消息解码类
 */
public class TcpSocketMessageProtocolDecoder extends ReplayingDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < IMConstants.MIN_READABLE_BYTES) {
            return;
        }
        int length = byteBuf.readInt();
        ByteBuf contentBuf = byteBuf.readBytes(length);
        String content = contentBuf.toString(StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        IMSendInfo imSendInfo = objectMapper.readValue(content, IMSendInfo.class);
        list.add(imSendInfo);
    }
}
