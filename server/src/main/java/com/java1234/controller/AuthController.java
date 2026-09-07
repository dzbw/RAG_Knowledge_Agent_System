package com.java1234.controller;

import com.java1234.common.R;
import com.java1234.dto.LoginRequest;
import com.java1234.dto.LoginResponse;
import com.java1234.entity.SystemLog;
import com.java1234.mapper.SystemLogMapper;
import com.java1234.service.AppUserService;
import com.java1234.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证：登录（JWT）。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;
    private final SystemLogMapper systemLogMapper;

    /**
     * 用户名密码登录，返回 Token。
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        LoginResponse resp = appUserService.login(req);
        SystemLog log = new SystemLog();
        log.setUserId(resp.getUser().getId());
        log.setAction("登录系统");
        log.setIp(WebUtils.clientIp(request));
        systemLogMapper.insert(log);
        return R.ok(resp);
    }
}
