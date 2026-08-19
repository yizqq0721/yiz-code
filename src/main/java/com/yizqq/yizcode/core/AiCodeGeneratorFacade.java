package com.yizqq.yizcode.core;

import com.yizqq.yizcode.ai.AiCodeGeneratorService;
import com.yizqq.yizcode.ai.model.HtmlCodeResult;
import com.yizqq.yizcode.ai.model.MultiFileCodeResult;
import com.yizqq.yizcode.exception.BusinessException;
import com.yizqq.yizcode.exception.ErrorCode;
import com.yizqq.yizcode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
      * @param userPrompt      用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userPrompt , CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userPrompt);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userPrompt);
            default -> {
                String ErrMessage = "不支持生成的类型"+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, ErrMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userPrompt      用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userPrompt , CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCodeStream(userPrompt);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userPrompt);
            default -> {
                String ErrMessage = "不支持生成的类型"+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, ErrMessage);
            }
        };
    }

    /**
     * 生成多文件代码并保存
     *
     * @param userPrompt 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userPrompt) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userPrompt);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }

    /**
     * 生成HTML代码并保存
     *
     * @param userPrompt 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userPrompt) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userPrompt);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成多文件代码并保存（流式）
     *
     * @param userPrompt 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userPrompt) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userPrompt);
        StringBuilder CodeBuilder = new StringBuilder();
        return stringFlux.doOnNext(check ->{
                CodeBuilder.append(check);
                }).doOnComplete(()->{
                    String completeMultiFileCode = CodeBuilder.toString();
                    MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                    File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
                    log.info("代码保存目录: {}", saveDir.getAbsolutePath());
                });
    }

    /**
     * 生成HTML代码并保存（流式）
     *
     * @param userPrompt 用户提示词
     * @return 保存的目录
     */

    private Flux<String> generateAndSaveHtmlCodeStream(String userPrompt) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCodeStream(userPrompt);
        StringBuilder CodeBuilder = new StringBuilder();
        return stringFlux.doOnNext(check ->{
            CodeBuilder.append(check);
        }).doOnComplete(()->{
            String completeHtmlCode = CodeBuilder.toString();
            HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
            File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
            log.info("代码保存目录: {}", saveDir.getAbsolutePath());
        });
    }

}
