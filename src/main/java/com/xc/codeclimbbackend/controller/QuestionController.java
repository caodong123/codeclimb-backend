package com.xc.codeclimbbackend.controller;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xc.codeclimbbackend.annotation.AuthCheck;
import com.xc.codeclimbbackend.common.BaseResponse;
import com.xc.codeclimbbackend.common.DeleteRequest;
import com.xc.codeclimbbackend.common.ErrorCode;
import com.xc.codeclimbbackend.common.ResultUtils;
import com.xc.codeclimbbackend.constant.UserConstant;
import com.xc.codeclimbbackend.exception.ThrowUtils;

import com.xc.codeclimbbackend.model.dto.question.QuestionAddRequest;
import com.xc.codeclimbbackend.model.dto.question.QuestionQueryRequest;
import com.xc.codeclimbbackend.model.dto.question.QuestionUpdateRequest;
import com.xc.codeclimbbackend.model.entity.Question;
import com.xc.codeclimbbackend.model.entity.User;
import com.xc.codeclimbbackend.model.vo.QuestionVO;
import com.xc.codeclimbbackend.service.QuestionService;
import com.xc.codeclimbbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    //  创建题目接口
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 将 QuestionAddRequest 转换为 Question 对象
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest,question);
        // 复制标签列表
        List<String> tags = questionAddRequest.getTags();
        if(tags != null){
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // todo 数据校验
        questionService.validQuestion(question,true);
        // 获取当前登录用户的 id
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        // 保存题目到数据库
        boolean saveResult = questionService.save(question);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存题目失败");
        // 返回新创建的题目 id
        Long questionId = question.getId();
        return ResultUtils.success(questionId);
    }

    // 删除题目接口
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        Long id = deleteRequest.getId();
        // 获取当前登录用户的 id
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅管理员或题目创建者可删除
        if (!question.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            ThrowUtils.throwIf(true, ErrorCode.NO_AUTH_ERROR);
        }
        boolean deleteResult = questionService.removeById(id);
        ThrowUtils.throwIf(!deleteResult, ErrorCode.SYSTEM_ERROR, "删除题目失败");
        return ResultUtils.success(true);
    }

    // 更新题目接口 仅管理员使用
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest){
        ThrowUtils.throwIf(questionUpdateRequest == null || questionUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将 QuestionUpdateRequest 转换为 Question 对象
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest,question);
        // 复制标签列表
        List<String> tags = questionUpdateRequest.getTags();
        if(tags != null){
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        //  数据校验
        questionService.validQuestion(question,false);
        // 判断是否存在
        Question oldQuestion = questionService.getById(question.getId());
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新题目
        boolean updateResult = questionService.updateById(question);
        ThrowUtils.throwIf(!updateResult, ErrorCode.SYSTEM_ERROR, "更新题目失败");
        return ResultUtils.success(true);
    }

    // 根据题目id获取题目接口（封装类）
    @PostMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id){
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取题目封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);
        return ResultUtils.success(questionVO);
    }

    //  分页获取题目列表接口（封装类）
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 限流
        int size = questionQueryRequest.getPageSize();
        if(size > 20){
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "每页最多20条数据");
        }
        // 获取题目分页列表
        Page<Question> questionPage =  questionService.listQuestionByPage(questionQueryRequest);
        // 获取题目封装分页列表
        Page<QuestionVO> questionVOList = questionService.getQuestionVOPage(questionPage, request);
        return ResultUtils.success(questionVOList);
    }

    // 分页获取题目列表 仅管理员使用
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取分页列表
        Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
        return ResultUtils.success(questionPage);
    }

    // 分页获取当前登录用户创建的题目列表 封装类
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户的 id
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        // 获取页数和每页条数
        int size = questionQueryRequest.getPageSize();
        int current = questionQueryRequest.getCurrent();
        // 获取题目分页列表
        Page<Question> questionPage = questionService.page(new Page<>(current, size), questionService.getQueryWrapper(questionQueryRequest));
        // 获取题目封装分页列表
        Page<QuestionVO> questionVOList = questionService.getQuestionVOPage(questionPage, request);
        return ResultUtils.success(questionVOList);
    }

}
