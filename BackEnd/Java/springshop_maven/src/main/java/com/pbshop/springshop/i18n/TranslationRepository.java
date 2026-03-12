package com.pbshop.springshop.i18n;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslationRepository extends JpaRepository<Translation, Long> {

    List<Translation> findAllByOrderByIdAsc();

    List<Translation> findByLocaleOrderByIdAsc(String locale);

    List<Translation> findByNamespaceOrderByIdAsc(String namespace);

    List<Translation> findByLocaleAndNamespaceOrderByIdAsc(String locale, String namespace);

    Optional<Translation> findByLocaleAndNamespaceAndKey(String locale, String namespace, String key);
}
