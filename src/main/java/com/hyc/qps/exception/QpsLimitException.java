package com.hyc.qps.exception;

/**
 * 类<code>QpsLimitException</code>说明：qps限流异常
 *
 * @author houyachao
 * @since 2023/8/7
 */
public class QpsLimitException extends RuntimeException {

    private int code;

    private String message;

    private long waitTime;

    public QpsLimitException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public QpsLimitException(int code, String message, long waitTime) {
        super(message);
        this.code = code;
        this.message = message;
        this.waitTime = waitTime;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public long getWaitTime() {
        return waitTime;
    }
}
