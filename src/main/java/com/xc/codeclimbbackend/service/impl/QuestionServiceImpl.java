package com.xc.codeclimbbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xc.codeclimbbackend.model.entity.Question;
import com.xc.codeclimbbackend.service.QuestionService;
import com.xc.codeclimbbackend.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author caodong
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2026-05-23 20:07:37
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




