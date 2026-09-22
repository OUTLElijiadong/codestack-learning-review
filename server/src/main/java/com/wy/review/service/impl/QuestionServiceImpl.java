package com.wy.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.BusinessException;
import com.wy.review.common.PageResult;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.dto.CommentDTO;
import com.wy.review.dto.QuestionDTO;
import com.wy.review.entity.Comment;
import com.wy.review.entity.Question;
import com.wy.review.entity.User;
import com.wy.review.mapper.CommentMapper;
import com.wy.review.mapper.QuestionMapper;
import com.wy.review.mapper.UserMapper;
import com.wy.review.service.QuestionService;
import com.wy.review.utils.SensitiveWordUtil;
import com.wy.review.vo.CommentVO;
import com.wy.review.vo.QuestionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 社区互助问答服务实现
 * 评论采用二级楼中楼：一级评论分页 + 按 root_id 批量查子回复，两次查询无递归
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final SensitiveWordUtil sensitiveWordUtil;

    public QuestionServiceImpl(QuestionMapper questionMapper, CommentMapper commentMapper,
                               UserMapper userMapper, SensitiveWordUtil sensitiveWordUtil) {
        this.questionMapper = questionMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.sensitiveWordUtil = sensitiveWordUtil;
    }

    @Override
    public void publish(QuestionDTO dto) {
        sensitiveWordUtil.assertClean(dto.getTitle() + " " + (dto.getContent() == null ? "" : dto.getContent()));
        Question q = new Question();
        q.setUserId(UserContext.getUserId());
        q.setTitle(dto.getTitle());
        q.setContent(dto.getContent());
        q.setTechDirection(dto.getTechDirection() == null || dto.getTechDirection().isEmpty()
                ? "其他" : dto.getTechDirection());
        q.setStatus(0);
        q.setViewCount(0);
        q.setAnswerCount(0);
        questionMapper.insert(q);
    }

    @Override
    public PageResult<QuestionVO> page(String keyword, String techDirection, Integer status,
                                       Integer my, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                // 列表不查 content 大字段
                .select(Question.class, field -> !"content".equals(field.getColumn()));
        if (my != null && my == 1) {
            // 只看自己提问
            wrapper.eq(Question::getUserId, UserContext.getUserId());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Question::getTitle, keyword.trim());
        }
        if (techDirection != null && !techDirection.isEmpty()) {
            wrapper.eq(Question::getTechDirection, techDirection);
        }
        if (status != null) {
            wrapper.eq(Question::getStatus, status);
        }
        wrapper.orderByDesc(Question::getCreateTime);
        Page<Question> page = questionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page, this::toVO);
    }

    @Override
    public QuestionVO detail(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        // 浏览数 +1，返回对象同步最新值（出参即数据库当前值）
        Question inc = new Question();
        inc.setId(q.getId());
        inc.setViewCount(q.getViewCount() + 1);
        questionMapper.updateById(inc);
        q.setViewCount(q.getViewCount() + 1);

        QuestionVO vo = toVO(q);
        vo.setContent(q.getContent());
        vo.setComments(buildCommentTree(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null || !q.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "只能删除自己的提问");
        }
        questionMapper.deleteById(id);
        // 连带评论逻辑删除：deleted 是 @TableLogic 字段，实体 update 不会进 SET 子句，
        // 必须用 mapper.delete(wrapper)（全局配置下自动生成 UPDATE SET deleted=1）
        commentMapper.delete(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getQuestionId, id));
    }

    @Override
    public void markSolved(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null || !q.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "只能操作自己的提问");
        }
        Question update = new Question();
        update.setId(id);
        update.setStatus(1);
        questionMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void comment(CommentDTO dto) {
        sensitiveWordUtil.assertClean(dto.getContent());
        Question q = questionMapper.selectById(dto.getQuestionId());
        if (q == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "问题不存在");
        }
        Comment c = new Comment();
        c.setQuestionId(dto.getQuestionId());
        c.setUserId(UserContext.getUserId());
        c.setContent(dto.getContent());

        // 二级楼中楼：parentId=0/空 → 一级评论；否则挂到一级评论（root）下
        Long parentId = dto.getParentId();
        if (parentId == null || parentId == 0) {
            c.setParentId(0L);
            c.setRootId(0L);
        } else {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null || !parent.getQuestionId().equals(dto.getQuestionId())) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "被回复的评论不存在");
            }
            c.setParentId(parentId);
            // 子回复统一挂到一级评论下，保证只有两级
            c.setRootId(parent.getRootId() == 0 ? parent.getId() : parent.getRootId());
            c.setReplyUserId(parent.getUserId());
        }
        commentMapper.insert(c);

        // 冗余回复数 +1，列表页免 COUNT
        Question update = new Question();
        update.setId(q.getId());
        update.setAnswerCount(q.getAnswerCount() + 1);
        questionMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeComment(Long id) {
        Comment c = commentMapper.selectById(id);
        if (c == null || !c.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "只能删除自己的评论");
        }
        commentMapper.deleteById(c.getId());
        Question q = questionMapper.selectById(c.getQuestionId());
        if (q != null && q.getAnswerCount() > 0) {
            Question update = new Question();
            update.setId(q.getId());
            update.setAnswerCount(q.getAnswerCount() - 1);
            questionMapper.updateById(update);
        }
    }

    @Override
    public PageResult<CommentVO> myComments(Integer pageNum, Integer pageSize) {
        // 只看自己回复
        Page<Comment> page = commentMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, UserContext.getUserId())
                        .orderByDesc(Comment::getCreateTime));
        // 批量取所属问题标题
        List<Long> qIds = page.getRecords().stream().map(Comment::getQuestionId).distinct()
                .collect(Collectors.toList());
        Map<Long, String> titleMap = qIds.isEmpty() ? Collections.emptyMap()
                : questionMapper.selectBatchIds(qIds).stream()
                .collect(Collectors.toMap(Question::getId, Question::getTitle));
        return PageResult.of(page, c -> {
            CommentVO vo = toCommentVO(c, userMapFor(Collections.singletonList(c)), Collections.emptyMap());
            vo.setQuestionTitle(titleMap.get(c.getQuestionId()));
            vo.setChildren(null);
            return vo;
        });
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /** 平铺评论组装成二级评论树 */
    private List<CommentVO> buildCommentTree(Long questionId) {
        List<Comment> all = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getQuestionId, questionId)
                .orderByAsc(Comment::getCreateTime));
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, User> userMap = userMapFor(all);
        Map<Long, String> nicknameMap = userMap.values().stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        List<CommentVO> roots = new ArrayList<>();
        Map<Long, CommentVO> rootIndex = new HashMap<>();
        for (Comment c : all) {
            CommentVO vo = toCommentVO(c, userMap, nicknameMap);
            if (c.getRootId() == 0) {
                roots.add(vo);
                rootIndex.put(vo.getId(), vo);
            } else {
                CommentVO root = rootIndex.get(c.getRootId());
                if (root != null) {
                    root.getChildren().add(vo);
                }
            }
        }
        return roots;
    }

    private Map<Long, User> userMapFor(List<Comment> comments) {
        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        return userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    private CommentVO toCommentVO(Comment c, Map<Long, User> userMap, Map<Long, String> nicknameMap) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setQuestionId(c.getQuestionId());
        vo.setParentId(c.getParentId());
        vo.setRootId(c.getRootId());
        vo.setContent(c.getContent());
        vo.setUserId(c.getUserId());
        vo.setCreateTime(c.getCreateTime());
        User u = userMap.get(c.getUserId());
        vo.setNickname(u == null ? "匿名同学" : u.getNickname());
        vo.setAvatar(u == null ? "" : u.getAvatar());
        vo.setRole(u == null ? "student" : u.getRole());
        if (c.getReplyUserId() != null) {
            vo.setReplyNickname(nicknameMap.getOrDefault(c.getReplyUserId(), "同学"));
        }
        return vo;
    }

    private QuestionVO toVO(Question q) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setTitle(q.getTitle());
        vo.setTechDirection(q.getTechDirection());
        vo.setStatus(q.getStatus());
        vo.setViewCount(q.getViewCount());
        vo.setAnswerCount(q.getAnswerCount());
        vo.setCreateTime(q.getCreateTime());
        vo.setUserId(q.getUserId());
        vo.setMine(q.getUserId().equals(UserContext.getUserId()));
        User author = userMapper.selectById(q.getUserId());
        vo.setNickname(author == null ? "匿名同学" : author.getNickname());
        vo.setAvatar(author == null ? "" : author.getAvatar());
        return vo;
    }
}
