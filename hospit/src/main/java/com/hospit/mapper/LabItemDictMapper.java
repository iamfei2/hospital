package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.LabItemDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LabItemDictMapper extends BaseMapper<LabItemDict> {

    // 根据项目代码查询
    @Select("SELECT * FROM lab_item_dict WHERE item_code = #{itemCode} LIMIT 1")
    LabItemDict selectByCode(String itemCode);

    // 根据项目名称查询
    @Select("SELECT * FROM lab_item_dict WHERE item_name = #{itemName} LIMIT 1")
    LabItemDict selectByName(String itemName);
}
