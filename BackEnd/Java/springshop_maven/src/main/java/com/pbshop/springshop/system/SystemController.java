package com.pbshop.springshop.system;

import com.pbshop.springshop.common.api.ApiResponse;
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
        return ApiResponse.success(
            Map.of(
                "status", "UP",
                "application", applicationName
            )
        );
    }
}
