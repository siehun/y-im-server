package io.yue.im.server.application.netty.runner;

import cn.hutool.core.collection.CollectionUtil;
import io.yue.im.server.application.netty.ImNettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

/**
 * 启动所有im服务
 */
@Component
public class IMServerRunner implements CommandLineRunner {

    @Autowired
    private List<ImNettyServer> imNettyServers;

    /**
     * 判断服务是否准备完毕
     * @return
     */
    public boolean isReady() {
        for (ImNettyServer imNettyServer : imNettyServers) {
            if (!imNettyServer.isReady()) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void run(String... args) throws Exception {
        if (!CollectionUtil.isEmpty(imNettyServers)) {
            imNettyServers.forEach(ImNettyServer::start);
        }
    }
    @PreDestroy
    public void destroy() {
        if (!CollectionUtil.isEmpty(imNettyServers)) {
            imNettyServers.forEach(ImNettyServer::shutdown);
        }
    }
}
