package io.yue.im.server.application.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.enums.IMCmdType;
import io.yue.im.common.domain.model.IMSendInfo;
import io.yue.im.server.application.netty.cache.UserChannelContextCache;
import io.yue.im.server.application.netty.processor.MessageProcessor;
import io.yue.im.server.application.netty.processor.factory.ProcessorFactory;
import io.yue.im.server.infrastructure.holder.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IMChannelHandler extends SimpleChannelInboundHandler<IMSendInfo> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMSendInfo imSendInfo) throws Exception {
        MessageProcessor processor = ProcessorFactory.getProcessor(IMCmdType.fromCode(imSendInfo.getCmd()));
        processor.process(ctx, processor.transForm(imSendInfo.getData()));

    }

    /**
     * 捕获通道处理过程中的所有异常,仅记录错误日志
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("IMChannelHandler.exceptionCaught|异常:{}", cause.getMessage());
    }


    /**
     *
     * @param ctx 当新连接建立时触发, 记录连接建立的日志（通道ID）
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("IMChannelHandler.handlerAdded|{}连接", ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(IMConstants.USER_ID);
        Long userId = ctx.channel().attr(userIdAttr).get();

        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(IMConstants.TERMINAL_TYPE);
        Integer terminal = ctx.channel().attr(terminalAttr).get();

        ChannelHandlerContext channelCtx = UserChannelContextCache.getChannelCtx(userId, terminal);
        // 防止异地登录误删
        // 确保缓存中存在该用户设备的连接记录 ,防止处理未认证连接或已清理的连接
        // 比较缓存中的连接ID与当前断开连接的ID,确保只清理当前实际失效的连接
        if (channelCtx != null && channelCtx.channel().id().equals(ctx.channel().id())){
            UserChannelContextCache.removeChannelCtx(userId, terminal);
            DistributedCacheService distributedCacheService = SpringContextHolder.getBean(IMConstants.DISTRIBUTED_CACHE_REDIS_SERVICE_KEY);
            String redisKey = String.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
            distributedCacheService.delete(redisKey);
            log.info("IMChannelHandler.handlerRemoved|断开连接, userId:{}, 终端类型:{}", userId, terminal);
        }
    }

    /**
     * 当触发IdleStateEvent.READER_IDLE（读空闲）时，判定为心跳超时。
     * 记录日志并主动关闭连接
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE){
                AttributeKey<Long> attr = AttributeKey.valueOf(IMConstants.USER_ID);
                Long userId = ctx.channel().attr(attr).get();

                AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(IMConstants.TERMINAL_TYPE);
                Integer terminal = ctx.channel().attr(terminalAttr).get();
                log.info("IMChannelHandler.userEventTriggered|心跳超时.即将断开连接, userId:{}, 终端类型:{}", userId, terminal);
                ctx.channel().close();
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
