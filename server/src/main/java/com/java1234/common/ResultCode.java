package com.java1234.common;

import lombok.Getter;

/**
 * 业务状态码与提示文案枚举。
 */
@Getter
public enum ResultCode {
    /** 成功 */
    OK(200, "操作成功"),
    /** 参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未登录 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 无权限 */
    FORBIDDEN(403, "无权限访问"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 服务器内部错误 */
    ERROR(500, "服务器内部错误");

    /** HTTP 语义化状态码 */
    private final int code;
    /** 默认提示信息 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
