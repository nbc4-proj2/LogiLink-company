package com.logilink.company.domain.company.service;

import com.logilink.company.common.exception.CompanyErrorCode;
import com.logilink.company.domain.company.model.entity.Company;
import com.logilink.company.domain.company.repository.CompanyRepository;
import com.sparta.logilinkcommon.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public Company createCompany(Company company) {
        if (companyRepository.existsByName(company.getName())) {
            throw AppException.of(CompanyErrorCode.COMPANY_NAME_DUPLICATE);
        }
        return companyRepository.save(company);
    }

    @Override
    @Transactional
    public Company updateCompany(UUID companyId, Company updatedCompany) {
        Company existingCompany = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        existingCompany.update(
                updatedCompany.getName(),
                updatedCompany.getAddress(),
                updatedCompany.getLatitude(),
                updatedCompany.getLongitude(),
                updatedCompany.getType());

        return existingCompany;

    }

    @Override
    public void deleteCompany(UUID companyId) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        company.delete(1L); // 추후 로그인 유저 ID로 교체
    }

    @Override
    public Company getCompany(UUID companyId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));
    }

    @Override
    public Page<Company> getCompanyPage(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return companyRepository.searchCompanies(keyword, pageable);
        }
        return companyRepository.findAllActive(pageable);
    }

}
