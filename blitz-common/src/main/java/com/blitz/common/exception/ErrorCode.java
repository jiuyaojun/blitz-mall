package com.blitz.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // System errors 1xxx
    SYSTEM_ERROR(1000, "系统内部错误"),
    PARAM_INVALID(1001, "参数校验失败"),
    NOT_FOUND(1002, "资源不存在"),
    METHOD_NOT_ALLOWED(1003, "请求方法不允许"),

    // Auth errors 2xxx
    UNAUTHORIZED(2000, "未登录或 Token 已过期"),
    FORBIDDEN(2001, "无权限访问"),
    TOKEN_EXPIRED(2002, "Token 已过期"),
    LOGIN_FAILED(2003, "用户名或密码错误"),
    ACCOUNT_DISABLED(2004, "账号已被禁用"),

    // Business errors 3xxx
    STOCK_INSUFFICIENT(3000, "库存不足"),
    ORDER_NOT_FOUND(3001, "订单不存在"),
    SECKILL_OVER(3002, "秒杀已结束"),
    SECKILL_REPEAT(3003, "你已经参与过该秒杀"),
    PAY_FAILED(3004, "支付失败"),

    // AI errors 4xxx
    AI_SERVICE_ERROR(4000, "AI 服务暂时不可用"),
    KNOWLEDGE_BASE_EMPTY(4001, "知识库为空");

    private final int code;
    private final String msg;
}
