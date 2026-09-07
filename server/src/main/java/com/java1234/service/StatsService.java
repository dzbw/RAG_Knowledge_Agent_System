package com.java1234.service;

import java.util.List;
import java.util.Map;

/**
 * 管理后台仪表盘统计。
 */
public interface StatsService {

    /**
     * 汇总指标 + 图表数据。
     */
    Map<String, Object> overview();
}
