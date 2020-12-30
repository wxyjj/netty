package com.example.netty.controller;


import com.alibaba.fastjson.JSONObject;
import com.example.netty.entity.NettyEntity;
import com.example.netty.redis.RedisService;
import com.example.netty.support.ApiException;
import com.example.netty.support.Result;
import com.example.netty.utils.CheckUtils;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static com.example.netty.NettyApplication.GLOBAL_GROUP;

/**
 * Netty服务
 *
 * @Author wxy
 * @Date 2020/12/28 13:45
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/netty")
public class NettyController {
    @Resource
    private RedisService redisService;

    /**
     * 发送通知(true:成功;false:失败)
     */
    @PostMapping(value = "/sendNotice")
    public Result<Boolean> sendNotice(@RequestBody NettyEntity nettyEntity) {
        log.info("GLOBAL_GROUP的数据" + GLOBAL_GROUP.size());
        CheckUtils.checkNull(nettyEntity, new ApiException(10000, "对象不能为空"));
        CheckUtils.checkNull(nettyEntity.getFromUserId(), new ApiException(10000, "发送者不能为空"));
        CheckUtils.checkNull(nettyEntity.getNoticeType(), new ApiException(10000, "通知不能为空"));
        CheckUtils.checkNull(nettyEntity.getTimeStamp(), new ApiException(10000, "时间戳不能为空"));
        //接受者
        List<String> toUserIds = nettyEntity.getToUserIds();
        //获取管道集合
        List<ChannelId> channelIds = redisService.findChannels(toUserIds);
        if (CollectionUtils.isEmpty(channelIds)) {
            log.info("接收者们都不在线");
            return Result.success(Boolean.FALSE);
        }
        nettyEntity.setToUserIds(null);
        String message = JSONObject.toJSONString(nettyEntity);
        TextWebSocketFrame tws = new TextWebSocketFrame(message);
        //确认目标们并发送
        channelIds.forEach(c -> GLOBAL_GROUP.find(c).writeAndFlush(tws));
        return Result.success(Boolean.TRUE);
    }
}
