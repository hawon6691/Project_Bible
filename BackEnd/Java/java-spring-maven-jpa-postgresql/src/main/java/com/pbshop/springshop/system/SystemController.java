package com.pbshop.springshop.system;

import com.pbshop.springshop.common.api.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class SystemController {

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("application", applicationName);
        payload.put("language", "java");
        return ApiResponse.success(payload);
    }
}
