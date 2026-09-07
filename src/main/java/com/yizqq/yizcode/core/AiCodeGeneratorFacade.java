package com.yizqq.yizcode.core;

import cn.hutool.json.JSONUtil;
import com.yizqq.yizcode.ai.AiCodeGeneratorService;
import com.yizqq.yizcode.ai.AiCodeGeneratorServiceFactory;
import com.yizqq.yizcode.ai.model.HtmlCodeResult;
import com.yizqq.yizcode.ai.model.MultiFileCodeResult;
import com.yizqq.yizcode.ai.model.message.AiResponseMessage;
import com.yizqq.yizcode.ai.model.message.ToolExecutedMessage;
import com.yizqq.yizcode.ai.model.message.ToolRequestMessage;
import com.yizqq.yizcode.constant.AppConstant;
import com.yizqq.yizcode.core.parser.CodeParserExecutor;
import com.yizqq.yizcode.core.save.CodeFileSaverExecutor;
import com.yizqq.yizcode.exception.BusinessException;
import com.yizqq.yizcode.exception.ErrorCode;
import com.yizqq.yizcode.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

@Slf4j
@Service
public class AiCodeGeneratorFacade {


    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码
     *
      * @param userPrompt      用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appID 应用ID
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userPrompt , CodeGenTypeEnum codeGenTypeEnum , Long appID) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据应用ID获取对应的AI代码生成服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appID, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userPrompt);
                yield  CodeFileSaverExecutor.executeSaver(htmlCodeResult,CodeGenTypeEnum.HTML,appID);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userPrompt);
                yield  CodeFileSaverExecutor.executeSaver(multiFileCodeResult,CodeGenTypeEnum.MULTI_FILE,appID);
            }
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
     * @param appID 应用ID
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userPrompt , CodeGenTypeEnum codeGenTypeEnum , Long appID) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据应用ID获取对应的AI代码生成服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appID, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userPrompt);
                yield  processCodeStream(codeStream, CodeGenTypeEnum.HTML, appID);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userPrompt);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appID);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream( appID ,userPrompt);
                yield processTokenStream(tokenStream);
                // Vue 项目文件由 AI 通过 FileWriteTool 直接写入项目目录，无需再经过解析和保存流程

            }
            default -> {
                String ErrMessage = "不支持生成的类型"+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, ErrMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
          /*              // 执行 Vue 项目构建（同步执行，确保预览时项目已就绪）
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);*/
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appId       应用 ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        // 字符串拼接器，用于当流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(() -> {
            // 流式返回完成后，保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }

}
