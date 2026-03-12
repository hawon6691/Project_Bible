package com.pbshop.springshop.prediction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.product.PriceEntry;
import com.pbshop.springshop.product.PriceEntryRepository;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;

@Service
@Transactional(readOnly = true)
public class PredictionService {

    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;

    public PredictionService(ProductRepository productRepository, PriceEntryRepository priceEntryRepository) {
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    public Map<String, Object> getPriceTrend(Long productId, Integer days) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        int window = days == null || days <= 0 ? 30 : Math.min(days, 90);
        List<PriceEntry> history = priceEntryRepository.findHistoryByProductIdSince(productId, OffsetDateTime.now().minusDays(window));
        if (history.isEmpty()) {
            history = priceEntryRepository.findByProductIdOrderByPriceAsc(productId);
        }
        if (history.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "가격 이력이 없습니다.");
        }

        BigDecimal currentPrice = history.get(history.size() - 1).getPrice();
        BigDecimal firstPrice = history.get(0).getPrice();
        BigDecimal movingAverage7d = average(last(history, 7));
        BigDecimal movingAverage30d = average(last(history, 30));
        BigDecimal delta = currentPrice.subtract(firstPrice);
        String trend = delta.signum() > 0 ? "RISING" : delta.signum() < 0 ? "FALLING" : "STABLE";
        BigDecimal trendStrength = ratio(delta.abs(), firstPrice);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productId", product.getId());
        response.put("productName", product.getName());
        response.put("currentPrice", currentPrice);
        response.put("predictions", buildPredictions(currentPrice, trend, window));
        response.put("trend", trend);
        response.put("trendStrength", trendStrength);
        response.put("movingAverage7d", movingAverage7d);
        response.put("movingAverage30d", movingAverage30d);
        response.put("seasonalityNote", "최근 가격 이력 기반 추세 예측입니다.");
        response.put("recommendation", recommendationCode(trend));
        response.put("recommendationReason", recommendationReason(trend));
        response.put("updatedAt", OffsetDateTime.now().toString());
        return response;
    }

    private List<PriceEntry> last(List<PriceEntry> history, int size) {
        if (history.size() <= size) {
            return history;
        }
        return history.subList(history.size() - size, history.size());
    }

    private BigDecimal average(List<PriceEntry> entries) {
        BigDecimal sum = entries.stream().map(PriceEntry::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(entries.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP).min(BigDecimal.ONE);
    }

    private List<Map<String, Object>> buildPredictions(BigDecimal currentPrice, String trend, int days) {
        BigDecimal calculatedStep = currentPrice.multiply(new BigDecimal("0.01")).setScale(0, RoundingMode.HALF_UP);
        final BigDecimal step = calculatedStep.signum() == 0 ? BigDecimal.ONE : calculatedStep;

        return List.of(1, 2, 3).stream()
                .map(day -> {
                    BigDecimal predictedPrice = switch (trend) {
                        case "FALLING" -> currentPrice.subtract(step.multiply(BigDecimal.valueOf(day)));
                        case "RISING" -> currentPrice.add(step.multiply(BigDecimal.valueOf(day)));
                        default -> currentPrice;
                    };
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("date", LocalDate.now().plusDays(Math.min(day, days)).toString());
                    response.put("predictedPrice", predictedPrice.max(BigDecimal.ZERO));
                    response.put("confidence", BigDecimal.valueOf(Math.max(0.55, 0.9 - (day * 0.08))).setScale(2, RoundingMode.HALF_UP));
                    return response;
                })
                .toList();
    }

    private String recommendationCode(String trend) {
        return switch (trend) {
            case "FALLING" -> "BUY_SOON";
            case "RISING" -> "HOLD";
            default -> "BUY_NOW";
        };
    }

    private String recommendationReason(String trend) {
        return switch (trend) {
            case "FALLING" -> "향후 단기 하락 가능성이 있어 추가 관찰 후 구매를 권장합니다.";
            case "RISING" -> "가격 상승 흐름이 감지되어 현재 가격 유지 시점 판단이 필요합니다.";
            default -> "큰 변동이 없어 현재 구매 시점으로 판단됩니다.";
        };
    }
}
