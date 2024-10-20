package com.yupi.codeclimb.mapper;

import com.yupi.codeclimb.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author CAODONG
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2024-09-27 15:04:04
* @Entity com.yupi.codeclimb.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查询题目列表（包括已被删除的数据）   因为mybatis-plus的逻辑删除功能会自动忽略逻辑删除的数据 所以需要手动查询所有数据
     */
    @Select("select * from question where updateTime >= #{minUpdateTime}")
    List<Question> listQuestionWithDelete(Date minUpdateTime);
}





