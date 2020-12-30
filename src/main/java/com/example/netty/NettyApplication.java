package com.example.netty;

import com.example.netty.netty.NettyServer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class NettyApplication {
	@Resource
	private NettyServer nettyServer;

	//分组最好丢在主程中
	public static final ChannelGroup GLOBAL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	public static void main(String[] args) {
		SpringApplication.run(NettyApplication.class, args);
	}

	@PostConstruct
	public void init() {
		CompletableFuture.runAsync(() -> nettyServer.run());
	}

}
