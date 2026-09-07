package com.java1234.service;

import com.java1234.common.PageResult;
import com.java1234.dto.LoginRequest;
import com.java1234.dto.LoginResponse;
import com.java1234.dto.UserSaveRequest;
import com.java1234.entity.AppUser;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户与登录业务。
 */
public interface AppUserService {

    /**
     * 用户名密码登录，密码与库中 MD5 比对。
     */
    LoginResponse login(LoginRequest req);

    AppUser getById(Long id);

    PageResult<AppUser> page(String keyword, int page, int size);

    void save(UserSaveRequest req);

    void delete(Long id);

    void updateProfile(Long userId, String realName);

    /**
     * 上传并更新头像。
     */
    String updateAvatar(Long userId, MultipartFile file);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
