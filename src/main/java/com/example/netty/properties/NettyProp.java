package com.example.netty.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author wxy
 * @Date 2020/10/12 9:21
 * @Version 1.0
 */
@Component
@ConfigurationProperties("netty")
public class NettyProp {

    private Integer port;

    private String path;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
