package com.java1234.mapper;

import com.java1234.entity.KbDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 知识文档 Mapper。
 */
@Mapper
public interface KbDocumentMapper {

    int insert(KbDocument row);

    int update(KbDocument row);

    KbDocument selectById(@Param("id") Long id);

    /**
     * 按主键批量查询分类（用于 RAG 检索：向量元数据可能缺少 categoryId 时从库表补全）。
     */
    List<KbDocument> selectIdCategoryByIds(@Param("ids") List<Long> ids);

    /**
     * 按分类批量反查文档主键（RAG 限定分类检索：避免依赖向量索引是否注册 categoryId TAG）。
     */
    List<Long> selectIdsByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    int deleteById(@Param("id") Long id);

    long countByKeyword(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    List<KbDocument> selectPage(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                               @Param("offset") int offset, @Param("limit") int limit);

    Long sumVectorCount();

    long countAll();

    List<Map<String, Object>> countGroupByCategory();

    int countByCategoryId(@Param("categoryId") Long categoryId);
}
