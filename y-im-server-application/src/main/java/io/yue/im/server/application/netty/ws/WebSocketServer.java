package io.yue.im.server.application.netty.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.yue.im.server.application.netty.ImNettyServer;
import io.yue.im.server.application.netty.handler.IMChannelHandler;
import io.yue.im.server.application.netty.ws.codec.WebSocketMessageProtocolDecoder;
import io.yue.im.server.application.netty.ws.codec.WebSocketMessageProtocolEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "websocket", value = "enable", havingValue = "true", matchIfMissing = true)
@Slf4j
public class WebSocketServer implements ImNettyServer {

    private volatile boolean ready = false;

    @Value("${websocket.port}")
    private int port;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    // 添加处理的Handler，通常包括消息编解码、业务处理，也可以是日志、权限、过滤等
                    @Override
                    protected void initChannel(Channel ch) {
                        // 获取职责链
                        ChannelPipeline pipeline = ch.pipeline();
                        // 120秒连接空闲检测，触发IdleStateEvent事件
                        pipeline.addLast(new IdleStateHandler(120, 0, 0, TimeUnit.SECONDS));
                        // HTTP协议编解码
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        // 聚合分块HTTP消息为完整报文（最大65KB）
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                        // 支持大文件分块传输
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        // 处理WebSocket握手、控制帧(Ping/Pong)，路径/im
                        pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                        pipeline.addLast("encode",  new WebSocketMessageProtocolEncoder());
                        pipeline.addLast("decode", new WebSocketMessageProtocolDecoder());
                        pipeline.addLast("handler", new IMChannelHandler());
                    }
                })
                // 等待连接队列最大长度
                .option(ChannelOption.SO_BACKLOG, 5)
                // 启用TCP保活机制
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try{
            bootstrap.bind(port).sync().channel();
            this.ready = true;
            log.info("webSocketServer.start|服务启动正常,监听端口:{}", port);
        }catch (InterruptedException e){
            log.error("webSocketServer.start|服务启动异常:{}", e.getMessage());
        }

    }

    @Override
    public void shutdown() {
        if (bossGroup != null && !bossGroup.isShuttingDown() && !bossGroup.isShutdown()){
            bossGroup.shutdownGracefully();
        }
        if (workGroup != null && !workGroup.isShuttingDown() && !workGroup.isShutdown()){
            workGroup.shutdownGracefully();
        }
        this.ready = false;
        log.info("webSocketServer.shutdown...服务停止");

    }
}
