package com.wy.review.controller.student;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.dto.CommentDTO;
import com.wy.review.dto.QuestionDTO;
import com.wy.review.service.QuestionService;
import com.wy.review.vo.CommentVO;
import com.wy.review.vo.QuestionVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 社区互助问答控制器（学生端）
 * 覆盖：发布问题、浏览问题、评论回复解答、只看自己提问、只看自己回复
 */
@RestController
@RequestMapping("/student/question")
@RequireRole("student")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** 发布编程疑难问题 */
    @PostMapping
    public Result<Void> publish(@Validated @RequestBody QuestionDTO dto) {
        questionService.publish(dto);
        return Result.ok();
    }

    /** 问题分页（my=1 时只看自己提问） */
    @GetMapping("/page")
    public Result<PageResult<QuestionVO>> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String techDirection,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) Integer my,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(questionService.page(keyword, techDirection, status, my, pageNum, pageSize));
    }

    /** 问题详情（含二级评论树） */
    @GetMapping("/{id}")
    public Result<QuestionVO> detail(@PathVariable Long id) {
        return Result.ok(questionService.detail(id));
    }

    /** 删除自己的提问 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        questionService.remove(id);
        return Result.ok();
    }

    /** 标记已解决 */
    @PutMapping("/{id}/solved")
    public Result<Void> markSolved(@PathVariable Long id) {
        questionService.markSolved(id);
        return Result.ok();
    }

    /** 评论回复解答（parentId 支持二级回复） */
    @PostMapping("/comment")
    public Result<Void> comment(@Validated @RequestBody CommentDTO dto) {
        questionService.comment(dto);
        return Result.ok();
    }

    /** 删除自己的评论 */
    @DeleteMapping("/comment/{id}")
    public Result<Void> removeComment(@PathVariable Long id) {
        questionService.removeComment(id);
        return Result.ok();
    }

    /** 只看自己回复：我的评论分页 */
    @GetMapping("/comment/my")
    public Result<PageResult<CommentVO>> myComments(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(questionService.myComments(pageNum, pageSize));
    }
}
