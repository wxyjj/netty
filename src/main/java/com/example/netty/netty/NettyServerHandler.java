package com.example.netty.netty;

import com.alibaba.fastjson.JSONObject;
import com.example.netty.entity.NettyEntity;
import com.example.netty.properties.NettyProp;
import com.example.netty.redis.RedisService;
import com.example.netty.support.ApiException;
import com.example.netty.utils.CheckUtils;
import com.example.netty.utils.SpringContextUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.example.netty.NettyApplication.GLOBAL_GROUP;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;

/**
 * @Author : wxy
 * @Date : 2020/12/26 11:49
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final RedisService redisService;
    private static final NettyProp nettyProp;
    private WebSocketServerHandshaker handshake;

    static {
        redisService = SpringContextUtils.getBean(RedisService.class);
        nettyProp = SpringContextUtils.getBean(NettyProp.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            log.info("以http请求形式接入走websocket");
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            log.info("以websocket接入");
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        //要求Upgrade为websocket，过滤掉get/Post
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            // 返回应答给客户端
            if (res.status().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
                res.content().writeBytes(buf);
                buf.release();
            }
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            // 如果是非Keep-Alive，关闭连接
            if (!isKeepAlive(req) || res.status().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }
        String websocketUrl = "ws://127.0.0.0:" + nettyProp.getPort() + nettyProp.getPath();
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(websocketUrl, null, false);
        handshake = wsFactory.newHandshaker(req);
        if (handshake == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshake.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshake.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            log.error("数据帧类型不支持!");
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
        // Send the uppercase string back.
        String request = ((TextWebSocketFrame) frame).text();
        log.info("Netty服务器接收到的信息: " + request);
        //数据获取并校验
        NettyEntity nettyEntity = JSONObject.parseObject(request, NettyEntity.class);
        CheckUtils.checkNull(nettyEntity.getTimeStamp(), new ApiException(10000, "参数timeStamp为空"));
        CheckUtils.checkNull(nettyEntity.getFromUserId(), new ApiException(10000, "参数fromUserId为空"));
        //发送者
        String fromUserId = nettyEntity.getFromUserId();
        //接受者
        List<String> toUserIds = nettyEntity.getToUserIds();
        //确认发送者位置
        ChannelId fromChannelIdId = redisService.findChannel(fromUserId);
        if (ObjectUtils.isEmpty(fromChannelIdId)) {
            redisService.insertChannel(fromUserId, ctx.channel().id());
        }
        //确认接收者位置;1.全部发送；2.部分发送；3.单体发送
        List<ChannelId> channelIds;
        if (CollectionUtils.isEmpty(toUserIds)) {
            channelIds = redisService.findChannels(fromUserId);
        } else {
            channelIds = redisService.findChannels(toUserIds);
        }
        if (CollectionUtils.isEmpty(channelIds)) {
            log.info("接收者们都不在线");
            return;
        }
        //有接受者在线,置空消息体中的接受者集合
        nettyEntity.setToUserIds(null);
        String message = JSONObject.toJSONString(nettyEntity);
        TextWebSocketFrame tws = new TextWebSocketFrame(message);
        //确认目标们并发送
        channelIds.forEach(c -> GLOBAL_GROUP.find(c).writeAndFlush(tws));
        log.info("向目标们发送成功!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    /**
     * 接收客户端连接事件
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("客户端与服务端连接开启：" + ctx.channel());
        GLOBAL_GROUP.add(ctx.channel());
    }

    /**
     * 接收客户端关闭事件
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客户端与服务端连接关闭：" + ctx.channel());
        GLOBAL_GROUP.remove(ctx.channel());
        redisService.deleteChannel(ctx.channel().id());
    }
}
