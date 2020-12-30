package com.example.netty.redis;

import io.netty.channel.ChannelId;

import java.util.List;

/**
 * redis操作 service层
 *
 * @Author wxy
 * @Date 2020/9/22 9:49
 * @Version 1.0
 */
public interface RedisService {
    /**
     * 管道存储
     */
    void insertChannel(String userId, ChannelId channelId);

    /**
     * 获取管道集合（排除自己）
     * userId为发送者
     */
    List<ChannelId> findChannels(String userId);

    /**
     * 获取管道集合
     * userIds为接受者
     */
    List<ChannelId> findChannels(List<String> userIds);

    /**
     * 获取某个管道
     */
    ChannelId findChannel(String userId);

    /**
     * 移除管道
     */
    void deleteChannel(ChannelId channelId);
}
