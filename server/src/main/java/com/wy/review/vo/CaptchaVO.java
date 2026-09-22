package com.wy.review.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 图形验证码返回：uuid 标识 + Base64 图片串 */
@Data
@AllArgsConstructor
public class CaptchaVO {

    /** 验证码标识（登录/注册时回传，服务端按此取答案比对） */
    private String uuid;

    /** Base64 编码的验证码图片（data:image/png;base64,...）前端直接渲染 */
    private String img;
}
