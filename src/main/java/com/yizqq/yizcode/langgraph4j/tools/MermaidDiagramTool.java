package com.yizqq.yizcode.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.yizqq.yizcode.exception.BusinessException;
import com.yizqq.yizcode.exception.ErrorCode;
import com.yizqq.yizcode.langgraph4j.model.ImageResource;
import com.yizqq.yizcode.langgraph4j.model.enums.ImageCategoryEnum;
import com.yizqq.yizcode.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mermaid 架构图生成工具
 */
@Slf4j
@Component
public class MermaidDiagramTool {

    /**
     * Puppeteer 配置文件路径。
     * 内容指向 Edge/Chrome 的可执行文件，避免 mmdc 去下载 Chromium。
     * 文件内容示例：
     * {
     *   "executablePath": "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
     *   "args": ["--no-sandbox", "--disable-setuid-sandbox"]
     * }
     */
    private static final String PUPPETEER_CONFIG =
            "D:\\code\\yiz-code\\src\\main\\\\resources\\\\MermaidDiagramToolConfig\\\\puppeteer-config.json";

    @Resource
    private CosManager cosManager;

    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }
        try {
            // 转换为SVG图片
            File diagramFile = convertMermaidToSvg(mermaidCode);
            // 上传到COS
            String keyName = String.format("/mermaid/%s/%s",
                    RandomUtil.randomString(5), diagramFile.getName());
            String cosUrl = cosManager.uploadFile(keyName, diagramFile);
            // 清理临时文件
            FileUtil.del(diagramFile);
            if (StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 将 Mermaid 代码转换为 SVG 图片
     */
    private File convertMermaidToSvg(String mermaidCode) {
        // 1. 写临时输入文件
        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        FileUtil.writeUtf8String(mermaidCode, tempInputFile);

        // 2. 准备输出文件：先删掉 Hutool 创建的空文件，让 mmdc 自己创建
        File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);
        FileUtil.del(tempOutputFile);

        // 3. 根据操作系统选择命令（Windows 必须用 mmdc.cmd）
        String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";

        // 4. 用参数列表构建命令，避免路径带空格被拆开
        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        cmd.add("-p");
        cmd.add(PUPPETEER_CONFIG);              // ★ 关键：指定 Edge/Chrome
        cmd.add("-i");
        cmd.add(tempInputFile.getAbsolutePath());
        cmd.add("-o");
        cmd.add(tempOutputFile.getAbsolutePath());

        log.info("执行 Mermaid 命令: {}", cmd);

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true); // 合并 stderr 到 stdout，方便看到真实报错
            Process process = pb.start();

            // 5. 读取子进程全部输出
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();
            log.info("Mermaid 退出码: {}, 输出: {}", exitCode, output);

            // 6. 用退出码 + 文件存在性双重判断
            if (exitCode != 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "Mermaid CLI 执行失败, exitCode=" + exitCode + ", 输出=" + output);
            }
            if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "Mermaid 输出文件为空, 输出=" + output);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "Mermaid CLI 执行异常: " + e.getMessage());
        }

        // 7. 清理输入文件，保留输出文件供上传使用
        FileUtil.del(tempInputFile);
        return tempOutputFile;
    }
}