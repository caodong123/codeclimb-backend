package com.xc.codeclimbbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xc.codeclimbbackend.common.ErrorCode;
import com.xc.codeclimbbackend.constant.CommonConstant;
import com.xc.codeclimbbackend.exception.ThrowUtils;
import com.xc.codeclimbbackend.model.dto.question.QuestionQueryRequest;
import com.xc.codeclimbbackend.model.entity.Question;
import com.xc.codeclimbbackend.model.entity.QuestionBankQuestion;
import com.xc.codeclimbbackend.model.entity.User;
import com.xc.codeclimbbackend.model.vo.QuestionVO;
import com.xc.codeclimbbackend.model.vo.UserVO;
import com.xc.codeclimbbackend.service.QuestionBankQuestionService;
import com.xc.codeclimbbackend.service.QuestionService;
import com.xc.codeclimbbackend.mapper.QuestionMapper;
import com.xc.codeclimbbackend.service.UserService;
import com.xc.codeclimbbackend.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author caodong
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2026-05-23 20:07:37
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

    @Autowired
    private QuestionBankQuestionService questionBankQuestionService;

    @Autowired
    private UserService userService;

    /**
     * 校验数据
     * @param question
     * @param add 是否为添加数据
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question==null, ErrorCode.PARAMS_ERROR);
        // 取出内容
        String title = question.getTitle();
        String content = question.getContent();
        // 创建题目时，标题不能为空
        if(add){
            ThrowUtils.throwIf(title==null || title.isEmpty(), ErrorCode.PARAMS_ERROR, "标题不能为空");
        }
        // 标题长度不能超过 100
        if(title != null){
            ThrowUtils.throwIf(title.length() > 100, ErrorCode.PARAMS_ERROR, "标题长度不能超过 100");
        }
        // 内容长度不能超过 1000
        if(content != null){
            ThrowUtils.throwIf(content.length() > 10240, ErrorCode.PARAMS_ERROR, "内容长度不能超过 1000");
        }

    }

    @Override
    public Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest) {
        // 校验分页参数
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = questionQueryRequest.getCurrent();
        long pageSize = questionQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0, ErrorCode.PARAMS_ERROR, "当前页码必须大于 0");
        ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "每页条数必须大于 0");
        // 获取queryWrapper
        QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);
        // 根据题库列表进行分页查询
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        if(questionBankId != null){
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .select(QuestionBankQuestion::getQuestionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionService.list(lambdaQueryWrapper);
            //如果不是空
            if(CollUtil.isNotEmpty(questionBankQuestionList)){
                // 取出id
                Set<Long> questionIdList = questionBankQuestionList.stream()
                        .map(QuestionBankQuestion::getQuestionId)
                        .collect(Collectors.toSet());
                queryWrapper.in("id", questionIdList);

            }else{
                // 题库中没有题目，直接返回空结果
                return new Page(current, pageSize, 0);
            }
        }
        // 分页查询
        Page<Question> questionPage = this.page(new Page<>(current, pageSize), queryWrapper);
        return questionPage;
    }

    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        String answer = questionQueryRequest.getAnswer();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        //判断集合是否为空
        if(CollUtil.isEmpty(questionList)){
            return questionVOPage;
        }
        List<QuestionVO> questionVOList = questionList.stream()
                .map(question -> {
                    return QuestionVO.objToVo(question);
                })
                .collect(Collectors.toList());
        //todo 关联查询用户信息
        Set<Long> userIdSet = questionVOList.stream()
                .map(QuestionVO::getUserId)
                .collect(Collectors.toSet());
        // 获取用户信息
        Map<Long, List<User>> userIdlistMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充用户信息
        questionVOList.forEach(
                questionVO->{
                    Long questionVOId = questionVO.getId();
                    if(userIdlistMap.containsKey(questionVOId)){
                        List<User> userList = userIdlistMap.get(questionVOId);
                        if(CollUtil.isNotEmpty(userList)){
                            User user = userList.get(0);
                            questionVO.setUser(userService.getUserVO(user));
                        }
                    }
                }
        );
        return questionVOPage.setRecords(questionVOList);
    }


}




