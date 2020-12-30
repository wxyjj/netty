package com.example.netty.netty;

import com.example.netty.properties.NettyProp;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author : wxy
 * @Date : 2020/12/26 11:46
 */
@Slf4j
@Service
public class NettyServer {
    @Resource
    private NettyProp nettyProp;

    public void run() {
        log.info("===============Netty服务端启动===============");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new NettyInitChannel());
            Channel ch = b.bind(nettyProp.getPort()).sync().channel();
            log.info("Netty服务端启动成功：" + ch.toString());
            ch.closeFuture().sync();
        } catch (Exception e) {
            log.error("Netty服务端运行异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("Netty服务端已关闭");
        }
    }

}
