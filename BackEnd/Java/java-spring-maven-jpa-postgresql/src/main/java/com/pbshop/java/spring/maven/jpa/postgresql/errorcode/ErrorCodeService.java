package com.pbshop.java.spring.maven.jpa.postgresql.errorcode;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ErrorCodeService {

    private final List<Map<String, Object>> items = List.of(
            Map.of("key", "FORBIDDEN", "status", 403, "message", "권한이 없습니다."),
            Map.of("key", "PRODUCT_NOT_FOUND", "status", 404, "message", "상품을 찾을 수 없습니다."),
            Map.of("key", "AUCTION_CLOSED", "status", 400, "message", "종료된 역경매입니다.")
    );

    public Map<String, Object> list() {
        return Map.of("total", items.size(), "items", items);
    }

    public Map<String, Object> show(String key) {
        return items.stream()
                .filter(item -> key.equals(item.get("key")))
                .findFirst()
                .orElse(Map.of("key", key, "status", 404, "message", "정의되지 않은 오류 코드입니다."));
    }
}
