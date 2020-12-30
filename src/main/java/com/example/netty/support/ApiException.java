package com.example.netty.support;

import javax.annotation.Resource;

/**
 * @Author wxy
 * @Date 2020/9/22 11:55
 * @Version 1.0
 */
public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 7201110362514121343L;
    @Resource
    private final IErrorCode errorCode;

    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(long code, String message) {
        super(message);
        this.errorCode = new IErrorCode() {
            @Override
            public long getCode() {
                return code;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    public IErrorCode getErrorCode() {
        return errorCode;
    }

}
