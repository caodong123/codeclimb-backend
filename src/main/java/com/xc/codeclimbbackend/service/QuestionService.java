package com.xc.codeclimbbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xc.codeclimbbackend.model.dto.question.QuestionQueryRequest;
import com.xc.codeclimbbackend.model.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xc.codeclimbbackend.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;


/**
* @author caodong
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2026-05-23 20:07:37
*/
public interface QuestionService extends IService<Question> {


    void validQuestion(Question question, boolean add);

    Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest);

    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);
}
