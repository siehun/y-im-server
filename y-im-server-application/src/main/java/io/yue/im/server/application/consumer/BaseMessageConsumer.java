package io.yue.im.server.application.consumer;

import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.model.IMReceiveInfo;


/**
 * @description 基础消息消费者
 */
public class BaseMessageConsumer {
    /**
     * 解析数据
     */
    protected IMReceiveInfo getReceiveMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMReceiveInfo.class);
    }
}
