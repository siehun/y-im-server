package io.yue.im.server.application.netty.processor.impl;

import io.netty.channel.ChannelHandlerContext;
import io.yue.im.common.domain.enums.IMCmdType;
import io.yue.im.common.domain.enums.IMSendCode;
import io.yue.im.common.domain.model.IMReceiveInfo;
import io.yue.im.common.domain.model.IMSendInfo;
import io.yue.im.common.domain.model.IMUserInfo;
import io.yue.im.common.mq.MessageSenderService;
import io.yue.im.server.application.netty.cache.UserChannelContextCache;
import io.yue.im.server.application.netty.processor.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description 私聊消息处理器
 */
@Component
public class PrivateMessageProcessor extends BaseMessageProcessor implements MessageProcessor<IMReceiveInfo> {
    private final Logger logger = LoggerFactory.getLogger(PrivateMessageProcessor.class);

    @Autowired
    private MessageSenderService messageSenderService;

    @Override
    public void process(IMReceiveInfo receiveInfo) {
        IMUserInfo sender = receiveInfo.getSender();
        IMUserInfo receiver = receiveInfo.getReceivers().get(0);
        logger.info("PrivateMessageProcessor.process|接收到消息,发送者:{}, 接收者:{}, 内容:{}", sender.getUserId(), receiver.getUserId(), receiveInfo.getData());
        try{
            ChannelHandlerContext channelHandlerContext = UserChannelContextCache.getChannelCtx(receiver.getUserId(), receiver.getTerminal());
            if (channelHandlerContext != null){
                //推送消息
                IMSendInfo<?> imSendInfo = new IMSendInfo<>(IMCmdType.PRIVATE_MESSAGE.code(), receiveInfo.getData());
                channelHandlerContext.writeAndFlush(imSendInfo);
                sendPrivateMessageResult(receiveInfo, IMSendCode.SUCCESS);
            }else{
                sendPrivateMessageResult(receiveInfo, IMSendCode.NOT_FIND_CHANNEL);
                logger.error("PrivateMessageProcessor.process|未找到Channel, 发送者:{}, 接收者:{}, 内容:{}", sender.getUserId(), receiver.getUserId(), receiveInfo.getData());
            }
        }catch (Exception e){
            sendPrivateMessageResult(receiveInfo, IMSendCode.UNKNOW_ERROR);
            logger.error("PrivateMessageProcessor.process|发送异常,发送者:{}, 接收者:{}, 内容:{}, 异常信息:{}", sender.getUserId(), receiver.getUserId(), receiveInfo.getData(), e.getMessage());
        }
    }
}
