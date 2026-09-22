package com.wy.review.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.wy.review.entity.Note;
import com.wy.review.mapper.NoteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 笔记定时备份任务：每天凌晨 2 点把全站笔记导出为 JSON 文件落盘 backups/ 目录
 * （cron 表达式可在 application.yml 的 review.backup.cron 调整）
 * 备份是文件级动作：导出文件即备份产物，学生端另有手动备份入口
 */
@Slf4j
@Component
public class NoteBackupTask {

    private final NoteMapper noteMapper;

    @Value("${review.backup.path}")
    private String backupPath;

    public NoteBackupTask(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    @Scheduled(cron = "${review.backup.cron}")
    public void backupAllNotes() {
        try {
            List<Note> notes = noteMapper.selectList(null);
            File dir = new File(backupPath).getAbsoluteFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = "notes-all-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".json";
            FileUtil.writeString(JSONUtil.toJsonPrettyStr(notes), new File(dir, fileName), StandardCharsets.UTF_8);
            log.info("笔记定时备份完成：{} 条笔记 → {}", notes.size(), fileName);
        } catch (Exception e) {
            log.warn("笔记定时备份失败：{}", e.getMessage());
        }
    }
}
