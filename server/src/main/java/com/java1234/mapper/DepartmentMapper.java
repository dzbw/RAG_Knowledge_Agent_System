package com.java1234.mapper;

import com.java1234.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门表 Mapper。
 */
@Mapper
public interface DepartmentMapper {

    List<Department> listAll();

    Department selectById(@Param("id") Long id);

    Department selectByName(@Param("name") String name);

    int insert(Department row);

    int update(Department row);

    int deleteById(@Param("id") Long id);
}
