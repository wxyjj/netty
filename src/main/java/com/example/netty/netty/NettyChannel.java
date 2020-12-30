package com.example.netty.netty;

import io.netty.channel.ChannelId;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author wxy
 * @Date 2020/12/28 10:59
 * @Version 1.0
 */
@Data
public class NettyChannel implements Serializable {
    private static final long serialVersionUID = -6113790961324726955L;

    private ChannelId channelId;

    private String asLongText;

    public NettyChannel() {
    }

    public NettyChannel(ChannelId channelId, String asLongText) {
        this.channelId = channelId;
        this.asLongText = asLongText;
    }
}
