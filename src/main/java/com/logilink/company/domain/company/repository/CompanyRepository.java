package com.logilink.company.domain.company.repository;

import com.logilink.company.domain.company.model.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    // 업체명 중복 검사
    boolean existsByName(String name);

    // 삭제 안 된 업체 단건 조회
    Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId);

    // 검색/페이징/정렬
    @Query("""
SELECT c 
FROM Company c
WHERE c.deletedAt IS NULL 
AND (
LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
OR LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')) 
)
""")
    Page<Company> searchCompanies(@Param("keyword") String keyword, Pageable pageable);

    // 키워드 없이 전체 조회 (deletedAt null들만)
    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL")
    Page<Company> findAllActive(Pageable pageable);

}
