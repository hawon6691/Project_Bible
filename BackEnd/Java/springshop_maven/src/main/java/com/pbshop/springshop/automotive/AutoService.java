package com.pbshop.springshop.automotive;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.automotive.dto.AutoDtos.EstimateAutoRequest;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;

@Service
@Transactional
public class AutoService {

    private final AutoModelRepository autoModelRepository;
    private final AutoTrimRepository autoTrimRepository;
    private final AutoOptionRepository autoOptionRepository;
    private final AutoLeaseOfferRepository autoLeaseOfferRepository;

    public AutoService(
            AutoModelRepository autoModelRepository,
            AutoTrimRepository autoTrimRepository,
            AutoOptionRepository autoOptionRepository,
            AutoLeaseOfferRepository autoLeaseOfferRepository
    ) {
        this.autoModelRepository = autoModelRepository;
        this.autoTrimRepository = autoTrimRepository;
        this.autoOptionRepository = autoOptionRepository;
        this.autoLeaseOfferRepository = autoLeaseOfferRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> models(String brand, String type) {
        return autoModelRepository.findByBrandContainingIgnoreCaseAndTypeContainingIgnoreCaseOrderByIdAsc(
                        brand == null ? "" : brand,
                        type == null ? "" : type)
                .stream()
                .map(model -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", model.getId());
                    item.put("brand", model.getBrand());
                    item.put("name", model.getName());
                    item.put("type", model.getType());
                    return item;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> trims(Long modelId) {
        return autoTrimRepository.findByAutoModelIdOrderByIdAsc(modelId).stream()
                .map(trim -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", trim.getId());
                    item.put("modelId", trim.getAutoModel().getId());
                    item.put("name", trim.getName());
                    item.put("basePrice", trim.getBasePrice().setScale(2, RoundingMode.HALF_UP));
                    return item;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> estimate(EstimateAutoRequest request) {
        AutoModel model = autoModelRepository.findById(request.modelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "차량 모델을 찾을 수 없습니다."));
        AutoTrim trim = autoTrimRepository.findById(request.trimId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "차량 트림을 찾을 수 없습니다."));
        if (!trim.getAutoModel().getId().equals(model.getId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "모델과 트림 조합이 올바르지 않습니다.");
        }
        List<AutoOption> options = request.optionIds() == null || request.optionIds().isEmpty()
                ? List.of()
                : autoOptionRepository.findByIdIn(request.optionIds());
        BigDecimal optionPrice = options.stream().map(AutoOption::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrice = trim.getBasePrice().add(optionPrice).setScale(2, RoundingMode.HALF_UP);
        List<Map<String, Object>> optionItems = options.stream()
                .map(option -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", option.getId());
                    item.put("name", option.getName());
                    item.put("price", option.getPrice().setScale(2, RoundingMode.HALF_UP));
                    return item;
                })
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("modelId", model.getId());
        response.put("modelName", model.getName());
        response.put("trimId", trim.getId());
        response.put("trimName", trim.getName());
        response.put("basePrice", trim.getBasePrice().setScale(2, RoundingMode.HALF_UP));
        response.put("optionPrice", optionPrice.setScale(2, RoundingMode.HALF_UP));
        response.put("totalPrice", totalPrice);
        response.put("options", optionItems);
        return response;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> leaseOffers(Long modelId) {
        return autoLeaseOfferRepository.findByAutoModelIdOrderByMonthlyPaymentAsc(modelId).stream()
                .map(offer -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", offer.getId());
                    item.put("modelId", offer.getAutoModel().getId());
                    item.put("provider", offer.getProvider());
                    item.put("monthlyPayment", offer.getMonthlyPayment().setScale(2, RoundingMode.HALF_UP));
                    item.put("contractMonths", offer.getContractMonths());
                    return item;
                })
                .toList();
    }
}
