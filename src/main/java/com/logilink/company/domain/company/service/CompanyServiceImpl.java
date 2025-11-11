package com.logilink.company.domain.company.service;

import com.logilink.company.common.exception.CompanyErrorCode;
import com.logilink.company.domain.company.model.dto.request.CompanyCreateRequest;
import com.logilink.company.domain.company.model.dto.request.CompanyUpdateRequest;
import com.logilink.company.domain.company.model.dto.response.CompanyResponse;
import com.logilink.company.domain.company.model.entity.Company;
import com.logilink.company.domain.company.repository.CompanyRepository;
import com.logilink.company.global.client.HubClient;
import com.sparta.logilinkcommon.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final HubClient hubClient;

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest companyCreateRequest) {

        if (!hubClient.existsHub(companyCreateRequest.getHubId())) {
            throw AppException.of(CompanyErrorCode.HUB_NOT_FOUND);
        }

        if (companyRepository.existsByName(companyCreateRequest.getName())) {
            throw AppException.of(CompanyErrorCode.COMPANY_NAME_DUPLICATE);
        }
        Company saved = companyRepository.save(companyCreateRequest.toEntity());
        return new CompanyResponse(saved);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(UUID companyId, CompanyUpdateRequest companyUpdateRequest) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        company.update(
                companyUpdateRequest.getName(),
                companyUpdateRequest.getAddress(),
                companyUpdateRequest.getLatitude(),
                companyUpdateRequest.getLongitude(),
                companyUpdateRequest.getType()
        );

        return new CompanyResponse(company);
    }

    @Override
    @Transactional
    public void deleteCompany(UUID companyId) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        company.delete(1L); // 나중에 로그인 사용자 ID로 교체
    }

    @Override
    public CompanyResponse getCompany(UUID companyId) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));
        return new CompanyResponse(company);
    }

    @Override
    public Page<CompanyResponse> getCompanyPage(String keyword, int page, int size, String sort) {
        List<Integer> allowedSizes = List.of(10, 30, 50);
        int finalSize = allowedSizes.contains(size) ? size : 10;

        String[] sortParam = sort.split(",");
        Sort.Direction direction = sortParam.length > 1 && sortParam[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, finalSize, Sort.by(direction, sortParam[0]));

        Page<Company> companies = (keyword != null && !keyword.isEmpty())
                ? companyRepository.searchCompanies(keyword, pageable)
                : companyRepository.findAllActive(pageable);

        return companies.map(CompanyResponse::new);
    }
}