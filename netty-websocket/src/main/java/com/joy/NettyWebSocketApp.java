package com.joy;

import com.joy.client.NettyWebSocketServer;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Joy
 */
@SpringBootApplication
public class NettyWebSocketApp implements CommandLineRunner {

    @Value("${netty.port}")
    private int nettyServerPort;
    @Resource
    private NettyWebSocketServer nettyWebSocketServer;

    public static void main(String[] args) {
        SpringApplication.run(NettyWebSocketApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        nettyWebSocketServer.start(nettyServerPort);
    }
}