package com.java1234.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 进项发票实体，对应表 t_invoice_in。
 */
@Data
public class InvoiceIn {
    private Long id;
    /** 发票号码 */
    private String invoiceNo;
    /** 发票类型：增值税专用发票/增值税普通发票/电子发票/其他 */
    private String invoiceType;
    /** 销售方（供应商） */
    private String supplier;
    /** 所属部门ID */
    private Long departmentId;
    /** 部门名称（查询时 JOIN 带出，入库不使用） */
    private String departmentName;
    /** 不含税金额 */
    private BigDecimal amount;
    /** 税额 */
    private BigDecimal taxAmount;
    /** 开票日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;
    /** 入账状态：0未入账 1已入账 */
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
