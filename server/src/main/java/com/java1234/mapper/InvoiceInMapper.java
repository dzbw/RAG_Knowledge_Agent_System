package com.java1234.mapper;

import com.java1234.entity.InvoiceIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 进项发票表 Mapper。
 */
@Mapper
public interface InvoiceInMapper {

    long count(@Param("keyword") String keyword,
               @Param("departmentId") Long departmentId,
               @Param("status") Integer status);

    List<InvoiceIn> selectPage(@Param("keyword") String keyword,
                               @Param("departmentId") Long departmentId,
                               @Param("status") Integer status,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    InvoiceIn selectById(@Param("id") Long id);

    int insert(InvoiceIn row);

    int update(InvoiceIn row);

    int deleteById(@Param("id") Long id);

    /**
     * 按部门统计：每个部门的张数与金额合计（含未入账的部门显示为 0 行）。
     * 返回字段：departmentId, deptName, count, totalAmount, totalTax。
     */
    List<Map<String, Object>> statByDepartment();

    long countByDepartmentId(@Param("departmentId") Long departmentId);
}
