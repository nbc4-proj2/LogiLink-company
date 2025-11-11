package com.logilink.company.domain.company.service;

import com.logilink.company.domain.company.model.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyService {
    Company createCompany(Company company);
    Company updateCompany(UUID companyId, Company updatedCompany);
    void deleteCompany(UUID companyId);
    Company getCompany(UUID companyId);
    Page<Company> getCompanyPage(String keyword, Pageable pageable);
}

