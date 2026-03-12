package com.pbshop.springshop.image;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageVariantRepository extends JpaRepository<ImageVariant, Long> {

    List<ImageVariant> findByImageAssetIdOrderByIdAsc(Long imageAssetId);

    void deleteByImageAssetId(Long imageAssetId);
}
