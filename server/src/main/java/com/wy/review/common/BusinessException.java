package com.wy.review.common;

import lombok.Getter;

/**
 * 业务异常：Service 层校验不通过时抛出，
 * 由 GlobalExceptionHandler 统一兜底转为 Result 返回
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String msg) {
        super(msg);
        this.resultCode = resultCode;
    }
}
