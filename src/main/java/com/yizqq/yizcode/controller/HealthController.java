package com.yizqq.yizcode.controller;


import com.yizqq.yizcode.common.BaseResponse;
import com.yizqq.yizcode.common.ResultUtils;
import com.yizqq.yizcode.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Health")
public class HealthController {

    @GetMapping
    public BaseResponse<String> healthcheck() {

        return ResultUtils.success("ok");
    }
}
