package com.example.netty.entity;

import com.example.netty.enums.NoticeType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * netty 通知
 *
 * @Author : wxy
 * @Date : 2020/12/26 12:44
 */
@Data
public class NettyEntity implements Serializable {
    private static final long serialVersionUID = 4173557964857117658L;

    /**
     * 发送者id
     */
    private String fromUserId;
    /**
     * 接收者id
     */
    private List<String> toUserIds;
    /**
     * 通知枚举
     */
    private NoticeType noticeType;
    /**
     * 内容
     */
    private String content;
    /**
     * 时间戳
     */
    private Long timeStamp;
}
