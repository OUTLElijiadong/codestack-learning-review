package com.wy.review.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wy.review.annotation.OperationLog;
import com.wy.review.common.LoginUser;
import com.wy.review.common.UserContext;
import com.wy.review.entity.OperationLogEntity;
import com.wy.review.mapper.OperationLogMapper;
import com.wy.review.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面：
 * 对所有标注 @OperationLog 的管理端方法做环绕通知，
 * 记录操作人/模块/动作/参数/IP/耗时/成败，写入 operation_log 表。
 * 日志写入全程 try-catch 包裹，绝不影响主业务流程。
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OperationLogAspect(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            try {
                saveLog(point, operationLog, error, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.warn("操作日志写入失败（不影响主流程）: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint point, OperationLog anno, Throwable error, long cost) {
        OperationLogEntity logEntity = new OperationLogEntity();
        LoginUser user = UserContext.get();
        if (user != null) {
            logEntity.setUserId(user.getId());
            logEntity.setUsername(user.getUsername());
            logEntity.setRole(user.getRole());
        }
        logEntity.setModule(anno.module());
        logEntity.setOperation(anno.operation());

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        logEntity.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            logEntity.setRequestMethod(request.getMethod());
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setIp(IpUtil.getClientIp(request));
        }

        // 参数摘要：剔除流对象，密码/密保等敏感字段脱敏
        logEntity.setParams(desensitize(point.getArgs()));

        logEntity.setCostTime(cost);
        if (error == null) {
            logEntity.setResult(1);
        } else {
            logEntity.setResult(0);
            String msg = error.getMessage();
            logEntity.setErrorMsg(msg != null && msg.length() > 500 ? msg.substring(0, 500) : msg);
        }
        operationLogMapper.insert(logEntity);
    }

    /** 参数序列化并脱敏：密码、密保答案绝不落日志 */
    private String desensitize(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            Map<String, Object> safe = new HashMap<>();
            for (Object arg : args) {
                if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof MultipartFile) {
                    continue;
                }
                if (arg == null) {
                    continue;
                }
                String json = objectMapper.writeValueAsString(arg);
                json = json.replaceAll("(\"(password|oldPassword|newPassword|securityAnswer)\"\\s*:\\s*\")[^\"]*\"", "$1******");
                safe.put(arg.getClass().getSimpleName(), json);
            }
            String text = safe.toString();
            return text.length() > 1000 ? text.substring(0, 1000) : text;
        } catch (Exception e) {
            return "(参数序列化失败)";
        }
    }
}
