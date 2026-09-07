package com.java1234.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 统计专用 Mapper（用户增长等）。
 */
@Mapper
public interface StatsMapper {

    /**
     * 近 7 天按日注册用户数量。
     */
    List<Map<String, Object>> countUserRegByDayLast7();
}
