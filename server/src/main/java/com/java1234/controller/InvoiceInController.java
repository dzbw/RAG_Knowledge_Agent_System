package com.java1234.controller;

import com.java1234.common.PageResult;
import com.java1234.common.R;
import com.java1234.dto.InvoiceInSaveRequest;
import com.java1234.entity.InvoiceIn;
import com.java1234.service.InvoiceInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 进项发票入账（财务管理，管理员）。
 */
@RestController
@RequestMapping("/api/invoices-in")
@RequiredArgsConstructor
public class InvoiceInController {

    private final InvoiceInService invoiceInService;

    /**
     * 分页查询发票明细（keyword=发票号/供应商，departmentId、status 可选）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public R<PageResult<InvoiceIn>> page(@RequestParam(defaultValue = "") String keyword,
                                         @RequestParam(required = false) Long departmentId,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return R.ok(invoiceInService.page(keyword, departmentId, status, page, size));
    }

    /**
     * 部门维度统计：各部门发票张数与金额合计。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stat-by-department")
    public R<List<Map<String, Object>>> statByDepartment() {
        return R.ok(invoiceInService.statByDepartment());
    }

    /**
     * 新增或更新发票（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public R<Void> save(@Valid @RequestBody InvoiceInSaveRequest req) {
        invoiceInService.save(req);
        return R.ok();
    }

    /**
     * 删除发票（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        invoiceInService.delete(id);
        return R.ok();
    }
}
