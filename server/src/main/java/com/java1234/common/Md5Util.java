package com.java1234.common;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * MD5 密码工具（与数据库中存取的 32 位小写 MD5 一致）。
 */
public final class Md5Util {

    private Md5Util() {
    }

    /**
     * 对原文进行 MD5 摘要，返回 32 位小写十六进制字符串。
     *
     * @param raw 明文
     * @return MD5 小写字符串
     */
    public static String md5Hex(String raw) {
        return DigestUtil.md5Hex(raw);
    }
}
