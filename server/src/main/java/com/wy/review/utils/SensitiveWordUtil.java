package com.wy.review.utils;

import com.wy.review.common.BusinessException;
import com.wy.review.common.ResultCode;
import com.wy.review.entity.SensitiveWord;
import com.wy.review.mapper.SensitiveWordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 敏感词工具：启动时把敏感词表全量加载进内存 Set，
 * 发布错题/笔记/问答/评论前在 Service 层调用 assertClean 校验，
 * 命中即抛业务异常，发布直接失败（不良关键词自动拦截）。
 * 词量为百级，内存 contains 匹配足够，不引入 DFA/Trie。
 */
@Slf4j
@Component
public class SensitiveWordUtil {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    /** 内存词库（线程安全） */
    private final Set<String> words = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        refresh();
    }

    /** 从数据库重新加载词库；表不存在（首次启动未执行 schema.sql）时只告警不中断启动 */
    public void refresh() {
        try {
            List<SensitiveWord> list = sensitiveWordMapper.selectList(null);
            words.clear();
            words.addAll(list.stream()
                    .filter(w -> w.getStatus() != null && w.getStatus() == 1)
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toSet()));
            log.info("敏感词库加载完成，共 {} 个词", words.size());
        } catch (Exception e) {
            log.warn("敏感词库暂未加载（首次启动时表还未建好，自动建表完成后会重新加载，无需处理）");
        }
    }

    /** 返回命中的敏感词，未命中返回 null */
    public String findHit(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        for (String word : words) {
            if (!word.isEmpty() && text.contains(word)) {
                return word;
            }
        }
        return null;
    }

    /** 校验文本，命中敏感词直接抛业务异常 */
    public void assertClean(String text) {
        String hit = findHit(text);
        if (hit != null) {
            throw new BusinessException(ResultCode.SENSITIVE_HIT,
                    ResultCode.SENSITIVE_HIT.getMsg() + "：" + hit);
        }
    }
}
