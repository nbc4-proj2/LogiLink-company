package com.logilink.company.domain.company.controller;

import com.logilink.company.domain.company.model.dto.request.CompanyCreateRequest;
import com.logilink.company.domain.company.model.dto.request.CompanyUpdateRequest;
import com.logilink.company.domain.company.model.dto.response.CompanyResponse;
import com.logilink.company.domain.company.model.entity.Company;
import com.logilink.company.domain.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Company API", description = "업체 관리 API")
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "업체 생성", description = "새로운 업체 생성")
    @PreAuthorize("hasAnyRole('MASTER_ADMIN', 'HUB_ADMIN')")
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyCreateRequest companyCreateRequest) {
        return ResponseEntity.ok(companyService.createCompany(companyCreateRequest));
    }

    @Operation(summary = "업체 수정", description = "업체의 이름, 주소, 위도, 경도, 업체 타입 수정")
    @PreAuthorize("hasAnyRole('MASTER_ADMIN', 'HUB_ADMIN', 'COMPANY_ADMIN')")
    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable UUID companyId, @Valid @RequestBody CompanyUpdateRequest companyUpdateRequest) {
        return ResponseEntity.ok(companyService.updateCompany(companyId, companyUpdateRequest));
    }

    @Operation(summary = "업체 삭제", description = "업체를 논리적으로 삭제")
    @PreAuthorize("hasAnyRole('MASTER_ADMIN', 'HUB_ADMIN')")
    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "업체 단건 조회", description = "업체 ID로 단일 조회")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(companyService.getCompany(companyId));
    }

    @Operation(summary = "업체 전체 조회", description = "검색어, 페이지, 정렬 조건을 기준으로 업체 조회")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> getCompanyPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(companyService.getCompanyPage(keyword, page, size, sort));

    }

}
