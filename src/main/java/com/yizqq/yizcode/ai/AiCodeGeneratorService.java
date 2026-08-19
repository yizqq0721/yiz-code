package com.yizqq.yizcode.ai;

import com.yizqq.yizcode.ai.model.HtmlCodeResult;
import com.yizqq.yizcode.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 生成HTML代码
     *
     * @param userPrompt 用户提示词
     * @return AI输出结果
     */
    @SystemMessage(fromResource = "/prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHTMLCode(String userPrompt);

    /**
     * 生成多文件代码
     *
     * @param userPrompt 用户提示词
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userPrompt);

    /**
     * 生成 HTML 代码
     * 流式输出
     *
     * @param userPrompt 用户提示词
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userPrompt);

    /**
     * 生成多文件代码
     * 流式输出
     *
     * @param userPrompt 用户提示词
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userPrompt);


/*    *//**
     * 生成 Vue 项目代码（流式）
     *
     * @param userPrompt 用户提示词
     * @return AI 的输出结果
     *//*
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userPrompt);*/
}
