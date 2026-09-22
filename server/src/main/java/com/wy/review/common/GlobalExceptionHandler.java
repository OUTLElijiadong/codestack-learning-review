package com.wy.review.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理：任何 Controller 抛出的异常都在这里统一兜底，
 * 保证前端永远收到 {code, msg, data} 结构，不会拿到 Tomcat 默认错误页
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常（主动抛出，msg 直接展示给用户） */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.fail(e.getResultCode().getCode(), e.getMessage());
    }

    /** @Validated 参数校验失败：取第一条校验信息返回 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValid(BindException e) {
        FieldError fe = e.getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : ResultCode.PARAM_ERROR.getMsg();
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /** 缺少必须的请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "缺少参数：" + e.getParameterName());
    }

    /** 上传文件超限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "文件大小超出限制（单文件最大 10MB）");
    }

    /** 参数类型不匹配（如 pageNum=abc）：按参数错误返回，不进兜底 500 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "参数格式不正确：" + e.getName());
    }

    /** 未知异常兜底：日志打完整堆栈，前端只给通用提示（不回传异常细节，防信息泄露） */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
    }
}
