package com.example.netty.enums;

/**
 * @Author : wxy
 * @Date : 2020/12/26 12:48
 */
public enum NoticeType {
    TEXT("TEXT","TEXT"),
    ;

    private final String name;
    private final String value;

    NoticeType(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
