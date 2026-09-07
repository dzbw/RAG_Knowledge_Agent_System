package com.java1234.mapper;

import com.java1234.entity.KbCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识分类 Mapper。
 */
@Mapper
public interface KbCategoryMapper {

    List<KbCategory> listAllOrderBySort();

    KbCategory selectById(@Param("id") Long id);

    int insert(KbCategory row);

    int update(KbCategory row);

    int deleteById(@Param("id") Long id);
}
