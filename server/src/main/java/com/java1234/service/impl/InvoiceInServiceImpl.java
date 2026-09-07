package com.java1234.service.impl;

import com.java1234.common.PageResult;
import com.java1234.dto.InvoiceInSaveRequest;
import com.java1234.entity.InvoiceIn;
import com.java1234.exception.BusinessException;
import com.java1234.mapper.DepartmentMapper;
import com.java1234.mapper.InvoiceInMapper;
import com.java1234.service.InvoiceInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * {@link InvoiceInService} 实现。
 */
@Service
@RequiredArgsConstructor
public class InvoiceInServiceImpl implements InvoiceInService {

    private final InvoiceInMapper invoiceInMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public PageResult<InvoiceIn> page(String keyword, Long departmentId, Integer status, int page, int size) {
        int off = Math.max(0, (page - 1) * size);
        long total = invoiceInMapper.count(keyword, departmentId, status);
        List<InvoiceIn> list = invoiceInMapper.selectPage(keyword, departmentId, status, off, size);
        return PageResult.of(total, list);
    }

    @Override
    public List<Map<String, Object>> statByDepartment() {
        return invoiceInMapper.statByDepartment();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(InvoiceInSaveRequest req) {
        if (departmentMapper.selectById(req.getDepartmentId()) == null) {
            throw new BusinessException("所属部门不存在");
        }
        if (req.getId() == null) {
            InvoiceIn row = new InvoiceIn();
            row.setInvoiceNo(req.getInvoiceNo());
            row.setInvoiceType(req.getInvoiceType());
            row.setSupplier(req.getSupplier());
            row.setDepartmentId(req.getDepartmentId());
            row.setAmount(req.getAmount());
            row.setTaxAmount(req.getTaxAmount() != null ? req.getTaxAmount() : java.math.BigDecimal.ZERO);
            row.setInvoiceDate(req.getInvoiceDate());
            row.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            row.setRemark(req.getRemark());
            invoiceInMapper.insert(row);
        } else {
            InvoiceIn db = invoiceInMapper.selectById(req.getId());
            if (db == null) {
                throw new BusinessException("发票记录不存在");
            }
            InvoiceIn row = new InvoiceIn();
            row.setId(req.getId());
            row.setInvoiceNo(req.getInvoiceNo());
            row.setInvoiceType(req.getInvoiceType());
            row.setSupplier(req.getSupplier());
            row.setDepartmentId(req.getDepartmentId());
            row.setAmount(req.getAmount());
            row.setTaxAmount(req.getTaxAmount() != null ? req.getTaxAmount() : java.math.BigDecimal.ZERO);
            row.setInvoiceDate(req.getInvoiceDate());
            row.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            row.setRemark(req.getRemark());
            invoiceInMapper.update(row);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (invoiceInMapper.selectById(id) == null) {
            return;
        }
        invoiceInMapper.deleteById(id);
    }
}
