package com.java1234.service.impl;

import com.java1234.mapper.AppUserMapper;
import com.java1234.mapper.ChatMessageMapper;
import com.java1234.mapper.KbDocumentMapper;
import com.java1234.mapper.StatsMapper;
import com.java1234.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link StatsService} 实现。
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final AppUserMapper appUserMapper;
    private final KbDocumentMapper kbDocumentMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final StatsMapper statsMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> overview() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userTotal", appUserMapper.countAll());
        m.put("documentTotal", kbDocumentMapper.countAll());
        m.put("vectorTotal", kbDocumentMapper.sumVectorCount() != null ? kbDocumentMapper.sumVectorCount() : 0L);
        m.put("qaToday", chatMessageMapper.countAssistantToday());
        m.put("qaByDay", fillLast7Days(chatMessageMapper.countAssistantByDayLast7()));
        m.put("categoryDocShare", kbDocumentMapper.countGroupByCategory());
        m.put("userRegByDay", fillLast7Days(statsMapper.countUserRegByDayLast7()));
        return m;
    }

    /**
     * 将近 7 日统计补全为连续日期（无数据则为 0）。
     */
    private static List<Map<String, Object>> fillLast7Days(List<Map<String, Object>> rows) {
        Map<String, Long> byDay = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                Object k = r.get("dayKey");
                Object c = r.get("cnt");
                if (k != null && c != null) {
                    byDay.put(k.toString(), ((Number) c).longValue());
                }
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String key = today.minusDays(i).toString();
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("date", key);
            one.put("count", byDay.getOrDefault(key, 0L));
            out.add(one);
        }
        return out;
    }
}
