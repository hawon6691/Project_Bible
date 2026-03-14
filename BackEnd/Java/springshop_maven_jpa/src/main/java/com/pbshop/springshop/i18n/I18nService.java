package com.pbshop.springshop.i18n;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.i18n.dto.I18nDtos.UpsertExchangeRateRequest;
import com.pbshop.springshop.i18n.dto.I18nDtos.UpsertTranslationRequest;

@Service
@Transactional
public class I18nService {

    private final TranslationRepository translationRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public I18nService(TranslationRepository translationRepository, ExchangeRateRepository exchangeRateRepository) {
        this.translationRepository = translationRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTranslations(String locale, String namespace) {
        List<Translation> translations;
        if (locale != null && !locale.isBlank() && namespace != null && !namespace.isBlank()) {
            translations = translationRepository.findByLocaleAndNamespaceOrderByIdAsc(locale, namespace);
        } else if (locale != null && !locale.isBlank()) {
            translations = translationRepository.findByLocaleOrderByIdAsc(locale);
        } else if (namespace != null && !namespace.isBlank()) {
            translations = translationRepository.findByNamespaceOrderByIdAsc(namespace);
        } else {
            translations = translationRepository.findAllByOrderByIdAsc();
        }
        return translations.stream().map(this::toTranslationResponse).toList();
    }

    public Map<String, Object> upsertTranslation(AuthenticatedUserPrincipal principal, UpsertTranslationRequest request) {
        requireAdmin(principal);
        Translation translation = translationRepository.findByLocaleAndNamespaceAndKey(
                        request.locale(), request.namespace(), request.key())
                .orElseGet(Translation::new);
        translation.setLocale(request.locale());
        translation.setNamespace(request.namespace());
        translation.setKey(request.key());
        translation.setValue(request.value());
        return toTranslationResponse(translationRepository.save(translation));
    }

    public Map<String, Object> deleteTranslation(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        Translation translation = translationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "번역을 찾을 수 없습니다."));
        translationRepository.delete(translation);
        return Map.of("message", "번역이 삭제되었습니다.");
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getExchangeRates() {
        return exchangeRateRepository.findAllByOrderByIdAsc().stream()
                .map(this::toExchangeRateResponse)
                .toList();
    }

    public Map<String, Object> upsertExchangeRate(AuthenticatedUserPrincipal principal, UpsertExchangeRateRequest request) {
        requireAdmin(principal);
        ExchangeRate rate = exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(
                        request.baseCurrency().toUpperCase(Locale.ROOT),
                        request.targetCurrency().toUpperCase(Locale.ROOT))
                .orElseGet(ExchangeRate::new);
        rate.setBaseCurrency(request.baseCurrency().toUpperCase(Locale.ROOT));
        rate.setTargetCurrency(request.targetCurrency().toUpperCase(Locale.ROOT));
        rate.setRate(request.rate().setScale(8, RoundingMode.HALF_UP));
        rate.setUpdatedAtExchange(OffsetDateTime.now());
        return toExchangeRateResponse(exchangeRateRepository.save(rate));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> convert(BigDecimal amount, String from, String to) {
        ExchangeRate rate = exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(
                        from.toUpperCase(Locale.ROOT),
                        to.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "환율 정보를 찾을 수 없습니다."));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("originalAmount", amount.setScale(2, RoundingMode.HALF_UP));
        response.put("originalCurrency", from.toUpperCase(Locale.ROOT));
        response.put("convertedAmount", amount.multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP));
        response.put("targetCurrency", to.toUpperCase(Locale.ROOT));
        response.put("rate", rate.getRate().setScale(8, RoundingMode.HALF_UP));
        return response;
    }

    private Map<String, Object> toTranslationResponse(Translation translation) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", translation.getId());
        response.put("locale", translation.getLocale());
        response.put("namespace", translation.getNamespace());
        response.put("key", translation.getKey());
        response.put("value", translation.getValue());
        return response;
    }

    private Map<String, Object> toExchangeRateResponse(ExchangeRate rate) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", rate.getId());
        response.put("baseCurrency", rate.getBaseCurrency());
        response.put("targetCurrency", rate.getTargetCurrency());
        response.put("rate", rate.getRate().setScale(8, RoundingMode.HALF_UP));
        response.put("updatedAt", (rate.getUpdatedAtExchange() == null ? rate.getUpdatedAt() : rate.getUpdatedAtExchange()).toString());
        return response;
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }
}
