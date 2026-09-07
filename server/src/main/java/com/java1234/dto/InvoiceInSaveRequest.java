package com.java1234.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增/编辑进项发票请求。
 */
@Data
public class InvoiceInSaveRequest {
    private Long id;
    @NotBlank(message = "发票号码不能为空")
    private String invoiceNo;
    @NotBlank(message = "发票类型不能为空")
    private String invoiceType;
    @NotBlank(message = "销售方不能为空")
    private String supplier;
    @NotNull(message = "所属部门不能为空")
    private Long departmentId;
    @NotNull(message = "不含税金额不能为空")
    private BigDecimal amount;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    @NotNull(message = "开票日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;
    /** 入账状态：0未入账 1已入账 */
    private Integer status = 1;
    private String remark;
}
