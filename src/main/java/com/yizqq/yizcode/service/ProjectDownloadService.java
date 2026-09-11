package com.yizqq.yizcode.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    /**
     * 下载项目为压缩包
     *
     * @param projectPath 项目根目录路径
     * @param downloadFileName 下载的文件名
     * @param response HttpServletResponse
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}