package com.logilink.company.domain.company.service;

import com.logilink.company.common.exception.CompanyErrorCode;
import com.logilink.company.domain.company.model.dto.request.CompanyCreateRequest;
import com.logilink.company.domain.company.model.dto.request.CompanyUpdateRequest;
import com.logilink.company.domain.company.model.dto.response.CompanyResponse;
import com.logilink.company.domain.company.model.entity.Company;
import com.logilink.company.domain.company.repository.CompanyRepository;
import com.logilink.company.global.client.HubClient;
import com.logilink.company.global.security.AuthHeaderExtractor;
import com.sparta.logilinkcommon.common.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final HubClient hubClient;
    private final AuthHeaderExtractor authHeaderExtractor;
    private final CompanyServiceImpl companyServiceImpl;

    // 현재 요청의 HttpServletRequest를 가져오는 헬퍼 메서드
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw AppException.of(CompanyErrorCode.REQUEST_CONTEXT_NOT_FOUND);
        }
        return attributes.getRequest();
    }

    // 업체 생성 시 허브 관리자의 소유권(담당 허브)을 검증하는 헬퍼 메서드
    private void validateHubAdminOwnership(UUID requestedHubId) {
        HttpServletRequest request = getCurrentRequest();
        String role = authHeaderExtractor.getUserRole(request);

        // 마스터 관리자 통과
        if ("MASTER_ADMIN".equals(role)) {
            return;
        }

        // 허브 관리자는 요청된 Hub ID와 자신의 Hub ID가 일치해야 함
        if ("HUB_ADMIN".equals(role)) {
            UUID userHubId = authHeaderExtractor.getHubId(request);
            if (!Objects.equals(userHubId, requestedHubId)) {
                throw AppException.of(CompanyErrorCode.ACCESS_DENIED_HUB_ADMIN_MISMATCH);
            }
        } else {
            throw AppException.of(CompanyErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest companyCreateRequest) {

        // 소유권 검증: HUB_ADMIN은 자신의 허브 ID 소속으로만 생성 가능
        validateHubAdminOwnership(companyCreateRequest.getHubId());

        if (!companyServiceImpl.isHubExistsAndCached(companyCreateRequest.getHubId())) {
            throw AppException.of(CompanyErrorCode.HUB_NOT_FOUND);
        }

        if (companyRepository.existsByName(companyCreateRequest.getName())) {
            throw AppException.of(CompanyErrorCode.COMPANY_NAME_DUPLICATE);
        }
        Company saved = companyRepository.save(companyCreateRequest.toEntity());
        return new CompanyResponse(saved);
    }

    @Cacheable(value = "hub-check", key = "#hubId")
    public boolean isHubExistsAndCached(UUID hubId) {
        return hubClient.existsHub(hubId);
    }

    // 업체 수정 시 여러 역할의 소유권을 복합적으로 검증하는 헬퍼 메서드
    private void validateCompanyUpdateAccess(Company company) {
        HttpServletRequest request = getCurrentRequest();
        String role = authHeaderExtractor.getUserRole(request);
        UUID userHubId = authHeaderExtractor.getHubId(request);
        UUID userCompanyId = authHeaderExtractor.getCompanyId(request);

        if ("MASTER_ADMIN".equals(role)) {
            return;
        }

        // HUB_ADMIN: 해당 업체의 Hub ID가 자신의 Hub ID와 일치하는 경우만 통과
        if ("HUB_ADMIN".equals(role)) {
            if (Objects.equals(company.getHubId(), userHubId)) {
                return;
            }
        }

        // COMPANY_ADMIN: 해당 업체의 ID가 자신의 Company ID와 일치하는 경우만 통과
        if ("COMPANY_ADMIN".equals(role)) {
            if (Objects.equals(company.getId(), userCompanyId)) {
                return;
            }
        }

        throw AppException.of(CompanyErrorCode.ACCESS_DENIED_COMPANY_MISMATCH);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(UUID companyId, CompanyUpdateRequest companyUpdateRequest) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        // HUB_ADMIN, COMPANY_ADMIN 모두 처리
        validateCompanyUpdateAccess(company);

        if (companyUpdateRequest.getName() != null
                && !companyUpdateRequest.getName().equals(company.getName())
                && companyRepository.existsByName(companyUpdateRequest.getName())) {
            throw AppException.of(CompanyErrorCode.COMPANY_NAME_DUPLICATE);
        }

        company.update(
                companyUpdateRequest.getName(),
                companyUpdateRequest.getAddress(),
                companyUpdateRequest.getLatitude(),
                companyUpdateRequest.getLongitude(),
                companyUpdateRequest.getType()
        );

        return new CompanyResponse(company);
    }

    // 업체 삭제 시 소유권 검증 (MASTER_ADMIN 또는 HUB_ADMIN)
    private void validateCompanyDeleteAccess(Company company) {
        HttpServletRequest request = getCurrentRequest();
        String role = authHeaderExtractor.getUserRole(request);
        UUID userHubId = authHeaderExtractor.getHubId(request);

        if ("MASTER_ADMIN".equals(role)) {
            return;
        }

        // HUB_ADMIN: 해당 업체의 Hub ID가 자신의 Hub ID와 일치하는 경우만 통과
        if ("HUB_ADMIN".equals(role)) {
            if (Objects.equals(company.getHubId(), userHubId)) {
                return;
            }
        }

        throw AppException.of(CompanyErrorCode.ACCESS_DENIED);
    }


    @Override
    @Transactional
    public void deleteCompany(UUID companyId) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        // 소유권 검증 로직
        validateCompanyDeleteAccess(company);

        Long deletedBy = authHeaderExtractor.getUserId(getCurrentRequest());

        if (deletedBy == null) {
            throw AppException.of(CompanyErrorCode.ACCESS_DENIED);
        }

        company.delete(deletedBy);
    }

    // COMPANY_ADMIN의 조회 범위 제한을 위한 헬퍼 메서드
    private Pageable filterPageableByRole(Pageable pageable) {
        HttpServletRequest request = getCurrentRequest();
        String role = authHeaderExtractor.getUserRole(request);

        if ("COMPANY_ADMIN".equals(role)) {
        }
        return pageable;
    }

    @Override
    public CompanyResponse getCompany(UUID companyId) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        HttpServletRequest request = getCurrentRequest();
        String role = authHeaderExtractor.getUserRole(request);
        UUID userCompanyId = authHeaderExtractor.getCompanyId(request);

        if ("COMPANY_ADMIN".equals(role)) {
            // 업체 담당자는 조회하려는 업체 ID가 자신의 Company ID와 일치하지 않으면 접근 거부
            if (!Objects.equals(company.getId(), userCompanyId)) {
                throw AppException.of(CompanyErrorCode.ACCESS_DENIED_COMPANY_MISMATCH);
            }
        }

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

        // COMPANY_ADMIN의 조회 범위를 제한
        HttpServletRequest request = getCurrentRequest();
        String role = authHeaderExtractor.getUserRole(request);

        if ("COMPANY_ADMIN".equals(role)) {

        }

        Page<Company> companies = (keyword != null && !keyword.isEmpty())
                ? companyRepository.searchCompanies(keyword, pageable)
                : companyRepository.findAllActive(pageable);

        return companies.map(CompanyResponse::new);
    }
}