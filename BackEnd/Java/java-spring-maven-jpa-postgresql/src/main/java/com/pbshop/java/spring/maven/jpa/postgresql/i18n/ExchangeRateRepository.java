package com.pbshop.java.spring.maven.jpa.postgresql.i18n;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    List<ExchangeRate> findAllByOrderByIdAsc();

    Optional<ExchangeRate> findByBaseCurrencyAndTargetCurrency(String baseCurrency, String targetCurrency);
}
