package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.IsolationForestModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IsolationForestModelMapper extends BaseMapper<IsolationForestModel> {

    // 根据规则ID查询模型
    IsolationForestModel selectModelByRuleId(@Param("ruleId") Long ruleId);

    // 根据项目ID列表查询最新模型
    IsolationForestModel selectLatestModelByItemIds(@Param("itemIds") String itemIds);

    // 插入模型
    int insertModel(IsolationForestModel model);

    // 更新模型
    int updateModel(IsolationForestModel model);
}