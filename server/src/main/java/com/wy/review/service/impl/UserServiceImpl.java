package com.wy.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.BusinessException;
import com.wy.review.common.PageResult;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.dto.AdminCreateDTO;
import com.wy.review.entity.CheckIn;
import com.wy.review.entity.Mistake;
import com.wy.review.entity.Note;
import com.wy.review.entity.User;
import com.wy.review.mapper.CheckInMapper;
import com.wy.review.mapper.MistakeMapper;
import com.wy.review.mapper.NoteMapper;
import com.wy.review.mapper.UserMapper;
import com.wy.review.service.UserService;
import com.wy.review.utils.PasswordUtil;
import com.wy.review.vo.UserInfoVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理服务实现（管理端）
 * 教师(teacher)与超管(admin)共用用户管理；管理员账号管理仅超管
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final MistakeMapper mistakeMapper;
    private final NoteMapper noteMapper;
    private final CheckInMapper checkInMapper;

    public UserServiceImpl(UserMapper userMapper, MistakeMapper mistakeMapper,
                           NoteMapper noteMapper, CheckInMapper checkInMapper) {
        this.userMapper = userMapper;
        this.mistakeMapper = mistakeMapper;
        this.noteMapper = noteMapper;
        this.checkInMapper = checkInMapper;
    }

    // ------------------------------------------------------------------
    // 全部用户统一管理
    // ------------------------------------------------------------------

    @Override
    public PageResult<UserInfoVO> page(String keyword, String role, Integer status,
                                       Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword.trim())
                    .or().like(User::getNickname, keyword.trim()));
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        // 教师（普通管理员）只管理学生，不展示超级管理员，防止误操作自身权限外账号
        if ("teacher".equals(com.wy.review.common.UserContext.getRole())) {
            wrapper.ne(User::getRole, "admin");
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page, UserInfoVO::of);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("info", UserInfoVO.of(user));
        // 学习数据摘要：错题数/笔记数/打卡天数
        result.put("mistakeTotal", mistakeMapper.selectCount(new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, id)));
        result.put("noteTotal", noteMapper.selectCount(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, id)));
        result.put("checkInTotal", checkInMapper.selectCount(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, id)));
        return result;
    }

    @Override
    public void freeze(Long id) {
        User target = checkManageable(id);
        User update = new User();
        update.setId(target.getId());
        update.setStatus(0); // 1正常 0冻结
        userMapper.updateById(update);
    }

    @Override
    public void unfreeze(Long id) {
        User target = checkManageable(id);
        User update = new User();
        update.setId(target.getId());
        update.setStatus(1);
        userMapper.updateById(update);
    }

    @Override
    public void resetPassword(Long id) {
        User target = checkManageable(id);
        User update = new User();
        update.setId(target.getId());
        update.setPassword(PasswordUtil.encode("123456"));
        userMapper.updateById(update);
    }

    @Override
    public Map<String, Object> activeStats() {
        Long studentTotal = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "student"));
        // 活跃口径：近 7 天有登录行为的学生
        Long activeWeek = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "student")
                .ge(User::getLastLoginTime, LocalDateTime.now().minusDays(7)));
        Long activeToday = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "student")
                .ge(User::getLastLoginTime, LocalDate.now().atStartOfDay()));
        Long frozenTotal = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "student")
                .eq(User::getStatus, 0));
        Map<String, Object> result = new HashMap<>();
        result.put("studentTotal", studentTotal);
        result.put("activeWeek", activeWeek);
        result.put("activeToday", activeToday);
        result.put("frozenTotal", frozenTotal);
        return result;
    }

    // ------------------------------------------------------------------
    // 管理员账号管理（仅超管）
    // ------------------------------------------------------------------

    @Override
    public PageResult<UserInfoVO> adminPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .in(User::getRole, "teacher", "admin");
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword.trim())
                    .or().like(User::getNickname, keyword.trim()));
        }
        // 教师（普通管理员）只管理学生，不展示超级管理员，防止误操作自身权限外账号
        if ("teacher".equals(com.wy.review.common.UserContext.getRole())) {
            wrapper.ne(User::getRole, "admin");
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page, UserInfoVO::of);
    }

    @Override
    public void createAdmin(AdminCreateDTO dto) {
        Long dup = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole("teacher"); // 新增的普通管理员固定为教师角色
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public void resetAdminPassword(Long id, String newPassword) {
        User target = userMapper.selectById(id);
        if (target == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if ("admin".equals(target.getUsername()) && !"admin".equals(currentUsername())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能重置内置超管的密码");
        }
        User update = new User();
        update.setId(id);
        update.setPassword(PasswordUtil.encode(newPassword));
        userMapper.updateById(update);
    }

    @Override
    public void deleteAdmin(Long id) {
        User target = userMapper.selectById(id);
        if (target == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if ("admin".equals(target.getUsername())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "内置超管账号不可删除");
        }
        if (target.getId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能删除当前登录的自己");
        }
        // 逻辑删除前先改名释放唯一键（uk_username 只约束有效行）：
        // username → username#del<id>，保证同名账号可再次注册，且操作日志仍可追溯原用户名
        User rename = new User();
        rename.setId(id);
        rename.setUsername(target.getUsername() + "#del" + id);
        userMapper.updateById(rename);
        userMapper.deleteById(id); // 逻辑删除
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /** 管理目标校验：目标存在、不是超管、不是自己 */
    private User checkManageable(Long id) {
        User target = userMapper.selectById(id);
        if (target == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if ("admin".equals(target.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能操作超级管理员");
        }
        if (target.getId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能操作当前登录的自己");
        }
        return target;
    }

    private String currentUsername() {
        return com.wy.review.common.UserContext.get() == null
                ? "" : com.wy.review.common.UserContext.get().getUsername();
    }
}
