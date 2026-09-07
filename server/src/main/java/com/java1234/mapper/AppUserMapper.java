package com.java1234.mapper;

import com.java1234.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 Mapper。
 */
@Mapper
public interface AppUserMapper {

    AppUser selectByUsername(@Param("username") String username);

    AppUser selectById(@Param("id") Long id);

    int insert(AppUser row);

    int update(AppUser row);

    int deleteById(@Param("id") Long id);

    long countByKeyword(@Param("keyword") String keyword);

    List<AppUser> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    long countAll();
}
