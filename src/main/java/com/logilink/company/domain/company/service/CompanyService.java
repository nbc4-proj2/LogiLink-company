package com.logilink.company.domain.company.service;

import com.logilink.company.domain.company.model.dto.request.CompanyCreateRequest;
import com.logilink.company.domain.company.model.dto.request.CompanyUpdateRequest;
import com.logilink.company.domain.company.model.dto.response.CompanyResponse;
import com.logilink.company.domain.company.model.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyService {
    CompanyResponse createCompany(CompanyCreateRequest companyCreateRequest);
    CompanyResponse updateCompany(UUID companyId, CompanyUpdateRequest companyUpdateRequest);
    void deleteCompany(UUID companyId);
    CompanyResponse getCompany(UUID companyId);
    Page<CompanyResponse> getCompanyPage(String keyword, int page, int size, String sort);
}

