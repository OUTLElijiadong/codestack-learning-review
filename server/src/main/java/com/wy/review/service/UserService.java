package com.wy.review.service;

import com.wy.review.common.PageResult;
import com.wy.review.dto.AdminCreateDTO;
import com.wy.review.vo.UserInfoVO;

import java.util.Map;

/**
 * 用户管理服务（管理端）：全部用户管理 + 管理员账号管理（权限分级）
 */
public interface UserService {

    // ---------------- 全部用户统一管理（teacher + admin） ----------------

    /** 用户分页：角色/状态/关键词筛选，密码密保不出参 */
    PageResult<UserInfoVO> page(String keyword, String role, Integer status, Integer pageNum, Integer pageSize);

    /** 用户详情：基础信息 + 学习数据摘要 */
    Map<String, Object> detail(Long id);

    /** 冻结异常账号（不能冻结超管与自己） */
    void freeze(Long id);

    /** 解封账号 */
    void unfreeze(Long id);

    /** 重置学生密码为初始值 123456（忘密码且忘密保的兜底） */
    void resetPassword(Long id);

    /** 活跃学习用户统计：学生总数/近7天活跃/今日活跃/冻结数 */
    Map<String, Object> activeStats();

    // ---------------- 管理员账号管理（仅 admin） ----------------

    /** 管理员账号列表（role 为 teacher/admin） */
    PageResult<UserInfoVO> adminPage(String keyword, Integer pageNum, Integer pageSize);

    /** 新增普通管理员（教师）账号 */
    void createAdmin(AdminCreateDTO dto);

    /** 重置管理员密码 */
    void resetAdminPassword(Long id, String newPassword);

    /** 删除管理员（超管自身与内置 admin 不可删） */
    void deleteAdmin(Long id);
}
