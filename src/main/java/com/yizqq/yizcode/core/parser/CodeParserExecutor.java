package com.yizqq.yizcode.core.parser;

import com.yizqq.yizcode.exception.BusinessException;
import com.yizqq.yizcode.exception.ErrorCode;
import com.yizqq.yizcode.model.enums.CodeGenTypeEnum;

public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    public static Object executeParser(String content , CodeGenTypeEnum codeGenTypeEnum) {

        return switch (codeGenTypeEnum)
        {
            case HTML -> htmlCodeParser.parseCode(content);
            case MULTI_FILE -> multiFileCodeParser.parseCode(content);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
