package com.java1234.service;

import com.java1234.common.PageResult;
import com.java1234.dto.InvoiceInSaveRequest;
import com.java1234.entity.InvoiceIn;

import java.util.List;
import java.util.Map;

/**
 * 进项发票业务。
 */
public interface InvoiceInService {

    /**
     * 分页查询发票明细（支持关键词、部门、入账状态筛选）。
     */
    PageResult<InvoiceIn> page(String keyword, Long departmentId, Integer status, int page, int size);

    /**
     * 部门维度统计：各部门发票张数、不含税金额合计、税额合计。
     */
    List<Map<String, Object>> statByDepartment();

    void save(InvoiceInSaveRequest req);

    void delete(Long id);
}
