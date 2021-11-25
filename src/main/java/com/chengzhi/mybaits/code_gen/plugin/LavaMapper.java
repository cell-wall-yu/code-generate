package com.chengzhi.mybaits.code_gen.plugin;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface LavaMapper<T extends LavaDo, E extends LavaExample> {

    void insertSelective(T record);

    int deleteByPrimaryKey(T record);

    int countByExample(E example);

    List<T> selectByExample(E example);

    int updateByExampleSelective(@Param("record") T record, @Param("example") E example);

    int updateByExampleSelective(Map<String, Object> map);

    int updateByExample(@Param("record") T record, @Param("example") E example);

    int updateByPrimaryKeySelective(T record);

    T selectByPrimaryKey(Long id);

}
