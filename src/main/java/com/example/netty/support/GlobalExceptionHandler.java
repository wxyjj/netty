package com.example.netty.support;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常拦截处理
 *
 * @Author wxy
 * @Date 2020/9/22 13:08
 * @Version 1.0
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public Result<Object> handle(ApiException apiException) {
        log.error(this.errInfo(apiException));
        IErrorCode errorCode = apiException.getErrorCode();
        return Result.failed(errorCode, errorCode.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result<Object> handle(Exception exception) {
        log.error(this.errInfo(exception));
        return Result.failed(exception.getMessage());
    }

    /**
     * 输出
     */
    public String errInfo(Exception e) {
        String str = "";
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            str = sw.toString();
        } catch (IOException ie) {
            log.error(ie.getMessage());
        }
        return str;
    }
}
