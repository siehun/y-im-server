package io.yue.im.server.application.netty.processor.impl;

import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.enums.IMSendCode;
import io.yue.im.common.domain.model.IMReceiveInfo;
import io.yue.im.common.domain.model.IMSendResult;
import io.yue.im.common.domain.model.IMUserInfo;
import io.yue.im.common.mq.MessageSenderService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @description 基础消息处理器
 */
public class BaseMessageProcessor {

    @Autowired
    private MessageSenderService messageSenderService;

    protected void sendPrivateMessageResult(IMReceiveInfo receiveInfo, IMSendCode sendCode){
        if (receiveInfo.getSendResult()){
            IMSendResult<?> result = new IMSendResult<>(receiveInfo.getSender(), receiveInfo.getReceivers().get(0), sendCode.code(), receiveInfo.getData());
            String sendKey = IMConstants.IM_RESULT_PRIVATE_QUEUE;
            result.setDestination(sendKey);
            messageSenderService.send(result);
        }
    }

    /**
     * 发送结果数据
     */
    protected void sendGroupMessageResult(IMReceiveInfo imReceivenfo, IMUserInfo imUserInfo, IMSendCode imSendCode){
        if (imReceivenfo.getSendResult()){
            IMSendResult<?> imSendResult = new IMSendResult<>(imReceivenfo.getSender(), imUserInfo, imSendCode.code(), imReceivenfo.getData());
            imSendResult.setDestination(IMConstants.IM_RESULT_GROUP_QUEUE);
            messageSenderService.send(imSendResult);
        }
    }
}
