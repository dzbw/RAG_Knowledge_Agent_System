package com.java1234.service.impl;

import com.java1234.dto.DepartmentSaveRequest;
import com.java1234.entity.Department;
import com.java1234.exception.BusinessException;
import com.java1234.mapper.DepartmentMapper;
import com.java1234.mapper.InvoiceInMapper;
import com.java1234.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link DepartmentService} 实现。
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final InvoiceInMapper invoiceInMapper;

    @Override
    public List<Department> listAll() {
        return departmentMapper.listAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(DepartmentSaveRequest req) {
        if (req.getId() == null) {
            if (departmentMapper.selectByName(req.getName()) != null) {
                throw new BusinessException("部门名称已存在");
            }
            Department d = new Department();
            d.setName(req.getName());
            d.setRemark(req.getRemark());
            departmentMapper.insert(d);
        } else {
            Department db = departmentMapper.selectById(req.getId());
            if (db == null) {
                throw new BusinessException("部门不存在");
            }
            if (!db.getName().equals(req.getName())) {
                Department same = departmentMapper.selectByName(req.getName());
                if (same != null) {
                    throw new BusinessException("部门名称已存在");
                }
            }
            Department d = new Department();
            d.setId(req.getId());
            d.setName(req.getName());
            d.setRemark(req.getRemark());
            departmentMapper.update(d);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (departmentMapper.selectById(id) == null) {
            return;
        }
        long used = invoiceInMapper.countByDepartmentId(id);
        if (used > 0) {
            throw new BusinessException("该部门下已有 " + used + " 张发票，不能删除");
        }
        departmentMapper.deleteById(id);
    }
}
