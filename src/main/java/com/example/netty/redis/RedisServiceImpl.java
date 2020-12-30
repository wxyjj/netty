package com.example.netty.redis;

import com.example.netty.netty.NettyChannel;
import com.example.netty.support.ApiException;
import com.example.netty.utils.CheckUtils;
import io.netty.channel.ChannelId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.netty.namespace.RedisNamespace.NETTY_CHANNEL;

/**
 * @Author wxy
 * @Date 2020/9/22 9:57
 * @Version 1.0
 */
public class RedisServiceImpl implements RedisService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 管道存储
     */
    @Override
    public void insertChannel(String userId, ChannelId channelId) {
        CheckUtils.checkNull(userId, new ApiException(10000, "用户id不能为空"));
        CheckUtils.checkNull(channelId, new ApiException(10000, "管道id不能为空"));
        Boolean bool = redisTemplate.hasKey(NETTY_CHANNEL + userId);
        if (bool) {
            redisTemplate.opsForValue().setIfPresent(NETTY_CHANNEL + userId, new NettyChannel(channelId, channelId.asLongText()));
        } else {
            redisTemplate.opsForValue().set(NETTY_CHANNEL + userId, new NettyChannel(channelId, channelId.asLongText()), 1, TimeUnit.DAYS);
        }
    }

    /**
     * 获取管道集合（排除自己）
     * userId为发送者
     */
    @Override
    public List<ChannelId> findChannels(String userId) {
        CheckUtils.checkNull(userId, new ApiException(10000, "用户id不能为空"));
        Set<String> set = redisTemplate.keys(NETTY_CHANNEL + "*");
        if (CollectionUtils.isEmpty(set)) {
            return new ArrayList<>();
        }
        String key = NETTY_CHANNEL + userId;
        return set.stream().filter(f -> !f.equals(key)).map(m -> {
            NettyChannel nettyChannel = (NettyChannel) redisTemplate.opsForValue().get(m);
            return nettyChannel.getChannelId();
        }).collect(Collectors.toList());
    }

    /**
     * 获取管道集合
     * userIds为接受者
     */
    @Override
    public List<ChannelId> findChannels(List<String> userIds) {
        CheckUtils.checkNull(userIds, new ApiException(10000, "用户id不能为空"));
        List<ChannelId> channelIds = new ArrayList<>();
        userIds.forEach(c -> {
            Object obj = redisTemplate.opsForValue().get(NETTY_CHANNEL + c);
            if (!ObjectUtils.isEmpty(obj)) {
                NettyChannel nettyChannel = (NettyChannel) obj;
                channelIds.add(nettyChannel.getChannelId());
            }
        });
        return channelIds;
    }

    /**
     * 获取某个管道
     */
    @Override
    public ChannelId findChannel(String userId) {
        CheckUtils.checkNull(userId, new ApiException(10000, "用户id不能为空"));
        Object obj = redisTemplate.opsForValue().get(NETTY_CHANNEL + userId);
        if (ObjectUtils.isEmpty(obj)) {
            return null;
        }
        NettyChannel nettyChannel = (NettyChannel) obj;
        return nettyChannel.getChannelId();
    }

    /**
     * 移除管道
     */
    @Override
    public void deleteChannel(ChannelId channelId) {
        CheckUtils.checkNull(channelId, new ApiException(10000, "管道id不能为空"));
        NettyChannel channel = new NettyChannel(channelId, channelId.asLongText());
        Set<String> set = redisTemplate.keys(NETTY_CHANNEL + "*");
        if (!CollectionUtils.isEmpty(set)) {
            set.forEach(c -> {
                NettyChannel nettyChannel = (NettyChannel) redisTemplate.opsForValue().get(c);
                assert nettyChannel != null;
                if (nettyChannel.getAsLongText().equals(channel.getAsLongText())) {
                    redisTemplate.delete(c);
                }
            });
        }
    }
}
