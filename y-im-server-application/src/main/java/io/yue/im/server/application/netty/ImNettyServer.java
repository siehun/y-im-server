package io.yue.im.server.application.netty;

public interface ImNettyServer {
    /**
     * 是否就绪
     * @return
     */
    boolean isReady();

    /**
     * 启动服务
     * @return
     */
    void start();

    /**
     * 停止服务
     */
    void shutdown();
}
