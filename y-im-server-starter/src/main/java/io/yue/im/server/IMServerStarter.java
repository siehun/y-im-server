package io.yue.im.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"io.yue.im", "com.alibaba.cola"})
@SpringBootApplication
public class IMServerStarter {
    public static void main(String[] args) {
        SpringApplication.run(IMServerStarter.class, args);
    }
}
