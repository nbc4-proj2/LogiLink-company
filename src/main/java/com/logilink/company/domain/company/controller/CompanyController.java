package com.logilink.company.domain.company.controller;

import com.logilink.company.domain.company.model.dto.request.CompanyCreateRequest;
import com.logilink.company.domain.company.model.dto.request.CompanyUpdateRequest;
import com.logilink.company.domain.company.model.dto.response.CompanyResponse;
import com.logilink.company.domain.company.model.entity.Company;
import com.logilink.company.domain.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyCreateRequest companyCreateRequest) {
        Company company = companyService.createCompany(companyCreateRequest.toEntity());
        return ResponseEntity.ok(new CompanyResponse(company));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable UUID companyId, @Valid @RequestBody CompanyUpdateRequest companyUpdateRequest) {
        Company company = companyService.updateCompany(companyId, companyUpdateRequest.toEntity());
        return ResponseEntity.ok(new CompanyResponse(company));
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getCompany(@PathVariable UUID companyId) {
        Company company = companyService.getCompany(companyId);
        return ResponseEntity.ok(new CompanyResponse(company));
    }

    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> getCompanyPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String keyword
    ) {

        List<Integer> allowedSizes = List.of(10, 30, 50);
        int finalSize = allowedSizes.contains(size) ? size : 10;

        String[] sortParam = sort.split(",");
        Sort.Direction direction = sortParam.length > 1 && sortParam[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, finalSize, Sort.by(direction, sortParam[0])); // finalSize 사용

        Page<Company> companies = companyService.getCompanyPage(keyword, pageable);

        Page<CompanyResponse> companyResponses = companies.map(CompanyResponse::new);
        return ResponseEntity.ok(companyResponses);

    }

}
