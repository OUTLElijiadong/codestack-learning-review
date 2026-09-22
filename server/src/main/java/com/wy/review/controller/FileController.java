package com.wy.review.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.wy.review.common.BusinessException;
import com.wy.review.common.Result;
import com.wy.review.common.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器（公共，任意登录角色）
 * 图片存本地 uploads/yyyy/MM/ 目录，返回可访问 URL（带 context-path /api 前缀），
 * 前端直接 <img src="返回的URL"> 即可经 vite proxy 访问
 */
@RestController
@RequestMapping("/common")
public class FileController {

    private static final String[] ALLOW_EXT = {"jpg", "jpeg", "png", "gif", "webp"};
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    @Value("${review.file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              HttpServletRequest request) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择要上传的文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件大小不能超过 10MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = FileUtil.extName(original).toLowerCase();
        if (!Arrays.asList(ALLOW_EXT).contains(ext)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持 jpg/png/gif/webp 图片");
        }

        // 按日期分目录存储；必须落绝对路径——MultipartFile.transferTo 遇到相对路径
        // 会解析到 Tomcat 临时工作目录，导致与静态资源映射目录不一致
        File baseDir = new File(uploadPath).getAbsoluteFile();
        String datePath = new SimpleDateFormat("yyyy/MM").format(new Date());
        File dir = new File(baseDir, datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = IdUtil.fastSimpleUUID() + "." + ext;
        file.transferTo(new File(dir, fileName).getAbsoluteFile());

        // 返回完整可访问路径：contextPath(/api) + 静态映射(/uploads) + 日期路径 + 文件名
        String url = request.getContextPath() + "/uploads/" + datePath + "/" + fileName;
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.ok(data);
    }
}
