package com.pbshop.springshop.errorcode;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/errors/codes")
public class ErrorCodeController {

    private final ErrorCodeService errorCodeService;

    public ErrorCodeController(ErrorCodeService errorCodeService) {
        this.errorCodeService = errorCodeService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list() {
        return ApiResponse.success(errorCodeService.list());
    }

    @GetMapping("/{key}")
    public ApiResponse<Map<String, Object>> show(@PathVariable String key) {
        return ApiResponse.success(errorCodeService.show(key));
    }
}
