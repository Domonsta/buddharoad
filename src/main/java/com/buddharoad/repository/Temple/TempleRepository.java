package com.buddharoad.repository.Temple;

import com.buddharoad.domain.Temple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TempleRepository extends JpaRepository<Temple, Long>, JpaSpecificationExecutor<Temple>{

    Page<Temple> findByIsDeletedFalse(Pageable pageable);
    Page<Temple> findByTempleNameContainingIgnoreCaseAndIsDeletedFalse(String templeName, Pageable pageable);
    Page<Temple> findByAddressContainingIgnoreCaseAndIsDeletedFalse(String address, Pageable pageable);
    Page<Temple> findByDescriptionContainingIgnoreCaseAndIsDeletedFalse(String description, Pageable pageable);

    // 쿼리 수정: regionNamesList가 null이거나 비어있을 때 조건을 무시하도록 수정
    @Query("SELECT t FROM Temple t WHERE t.isDeleted = false " +
            "AND (:templeName IS NULL OR LOWER(t.templeName) LIKE %:templeName%) " + // ⭐ LOWER() 적용
            "AND (:feature IS NULL OR LOWER(t.feature) LIKE %:feature%) " +
            "AND (:#{#regionNamesList == null || #regionNamesList.isEmpty()} = true OR t.region IN :regionNamesList)") // ⭐ 리스트가 null이거나 비어있을 때 처리
    Page<Temple> findByFiltersWithMultipleRegions(@Param("templeName") String templeName,
                                                  @Param("feature") String feature,
                                                  @Param("regionNamesList") List<String> regionNamesList,
                                                  Pageable pageable);
}