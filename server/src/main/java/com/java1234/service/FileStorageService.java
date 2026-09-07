package com.java1234.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 将上传文件保存到本地磁盘（D:/uploads3 下按年月分子目录）。
 */
public interface FileStorageService {

    /**
     * 保存上传文件并返回【相对 upload 根目录】的路径片段，例如 {@code 202605/xxx.pdf}。
     *
     * @param file 上传文件
     * @return 相对路径与磁盘绝对路径
     * @throws IOException IO 异常
     */
    StoredFile save(MultipartFile file) throws IOException;

    /**
     * 已保存文件描述。
     *
     * @param relativePath 相对 uploads 根的路径
     * @param absolutePath 绝对路径
     */
    record StoredFile(String relativePath, Path absolutePath) {
    }
}
