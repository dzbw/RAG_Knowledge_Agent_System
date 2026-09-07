package com.java1234.mapper;

import com.java1234.entity.SystemLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志 Mapper。
 */
@Mapper
public interface SystemLogMapper {

    int insert(SystemLog row);
}
