package com.java1234.service.impl;

import com.java1234.common.JwtUtil;
import com.java1234.common.Md5Util;
import com.java1234.common.PageResult;
import com.java1234.dto.LoginRequest;
import com.java1234.dto.LoginResponse;
import com.java1234.dto.UserSaveRequest;
import com.java1234.entity.AppUser;
import com.java1234.exception.BusinessException;
import com.java1234.mapper.AppUserMapper;
import com.java1234.service.AppUserService;
import com.java1234.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * {@link AppUserService} 实现。
 */
@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private static final Set<String> AVATAR_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final AppUserMapper appUserMapper;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResponse login(LoginRequest req) {
        AppUser u = appUserMapper.selectByUsername(req.getUsername());
        if (u == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (u.getStatus() != null && u.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }
        String hash = Md5Util.md5Hex(req.getPassword());
        if (!hash.equals(u.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.createToken(u.getId(), u.getUsername(), u.getRole());
        LoginResponse.UserBrief brief = new LoginResponse.UserBrief(
                u.getId(), u.getUsername(), u.getRealName(), u.getRole(), u.getAvatar());
        return new LoginResponse(token, brief);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUser getById(Long id) {
        return appUserMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<AppUser> page(String keyword, int page, int size) {
        int off = Math.max(0, (page - 1) * size);
        long total = appUserMapper.countByKeyword(keyword);
        List<AppUser> list = appUserMapper.selectPage(keyword, off, size);
        return PageResult.of(total, list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(UserSaveRequest req) {
        if (req.getId() == null) {
            AppUser exists = appUserMapper.selectByUsername(req.getUsername());
            if (exists != null) {
                throw new BusinessException("用户名已存在");
            }
            AppUser u = new AppUser();
            u.setUsername(req.getUsername());
            u.setPassword(Md5Util.md5Hex(req.getPassword() != null && !req.getPassword().isEmpty()
                    ? req.getPassword() : "123456"));
            u.setRealName(req.getRealName());
            u.setRole(req.getRole());
            u.setStatus(req.getStatus());
            appUserMapper.insert(u);
        } else {
            AppUser db = appUserMapper.selectById(req.getId());
            if (db == null) {
                throw new BusinessException("用户不存在");
            }
            if (!db.getUsername().equals(req.getUsername())) {
                AppUser exists = appUserMapper.selectByUsername(req.getUsername());
                if (exists != null) {
                    throw new BusinessException("用户名已存在");
                }
            }
            AppUser u = new AppUser();
            u.setId(req.getId());
            u.setUsername(req.getUsername());
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                u.setPassword(Md5Util.md5Hex(req.getPassword()));
            }
            u.setRealName(req.getRealName());
            u.setRole(req.getRole());
            u.setStatus(req.getStatus());
            appUserMapper.update(u);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AppUser u = appUserMapper.selectById(id);
        if (u == null) {
            return;
        }
        if ("ADMIN".equalsIgnoreCase(u.getRole()) && "admin".equalsIgnoreCase(u.getUsername())) {
            throw new BusinessException("不能删除内置管理员");
        }
        appUserMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, String realName) {
        AppUser u = new AppUser();
        u.setId(userId);
        u.setRealName(realName);
        appUserMapper.update(u);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片文件");
        }
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        int dot = name.lastIndexOf('.');
        String ext = dot > 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        if (!AVATAR_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg、jpeg、png、gif、webp 图片");
        }
        if (appUserMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }
        String relative;
        try {
            relative = fileStorageService.save(file).relativePath();
        } catch (Exception e) {
            throw new BusinessException("头像保存失败");
        }
        AppUser u = new AppUser();
        u.setId(userId);
        u.setAvatar(relative);
        appUserMapper.update(u);
        return relative;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        AppUser db = appUserMapper.selectById(userId);
        if (db == null) {
            throw new BusinessException("用户不存在");
        }
        if (!Md5Util.md5Hex(oldPassword).equals(db.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        AppUser u = new AppUser();
        u.setId(userId);
        u.setPassword(Md5Util.md5Hex(newPassword));
        appUserMapper.update(u);
    }
}
