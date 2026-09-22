package com.wy.review.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回体：所有接口固定返回 {code, msg, data} 三段结构
 * 前端 Axios 响应拦截器按 code === 200 判定业务成功
 */
@Data
public class Result<T> implements Serializable {

    /** 业务状态码：200 成功，其余见 ResultCode 枚举 */
    private Integer code;
    /** 提示信息（成功/失败原因） */
    private String msg;
    /** 业务数据 */
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMsg(ResultCode.SUCCESS.getMsg());
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(ResultCode rc) {
        return fail(rc.getCode(), rc.getMsg());
    }

    public static <T> Result<T> fail(ResultCode rc, String msg) {
        return fail(rc.getCode(), msg);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
