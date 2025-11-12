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

        // 마스터 관리자는 검증 통과
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
            // Controller에서 이미 MASTER_ADMIN, HUB_ADMIN만 통과시켰지만, 혹시 모를 경우를 대비하여 방어 코드
            throw AppException.of(CompanyErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest companyCreateRequest) {

        // 1. 소유권 검증: HUB_ADMIN은 자신의 허브 ID 소속으로만 생성 가능
        validateHubAdminOwnership(companyCreateRequest.getHubId());

        if (!hubClient.existsHub(companyCreateRequest.getHubId())) {
            throw AppException.of(CompanyErrorCode.HUB_NOT_FOUND);
        }

        if (companyRepository.existsByName(companyCreateRequest.getName())) {
            throw AppException.of(CompanyErrorCode.COMPANY_NAME_DUPLICATE);
        }
        Company saved = companyRepository.save(companyCreateRequest.toEntity());
        return new CompanyResponse(saved);
    }

    // 업체 수정 시 여러 역할의 소유권을 복합적으로 검증하는 헬퍼 메서드
    private void validateCompanyUpdateAccess(Company company) {
        HttpServletRequest request = getCurrentRequest();
        String role = authHeaderExtractor.getUserRole(request);
        UUID userHubId = authHeaderExtractor.getHubId(request);
        UUID userCompanyId = authHeaderExtractor.getCompanyId(request);

        // 1. MASTER_ADMIN: 무조건 통과
        if ("MASTER_ADMIN".equals(role)) {
            return;
        }

        // 2. HUB_ADMIN: 해당 업체의 Hub ID가 자신의 Hub ID와 일치하는 경우만 통과
        if ("HUB_ADMIN".equals(role)) {
            if (Objects.equals(company.getHubId(), userHubId)) {
                return;
            }
        }

        // 3. COMPANY_ADMIN: 해당 업체의 ID가 자신의 Company ID와 일치하는 경우만 통과
        if ("COMPANY_ADMIN".equals(role)) {
            if (Objects.equals(company.getId(), userCompanyId)) {
                return;
            }
        }

        // 위 모든 조건에 해당하지 않으면 접근 거부
        throw AppException.of(CompanyErrorCode.ACCESS_DENIED_COMPANY_MISMATCH);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(UUID companyId, CompanyUpdateRequest companyUpdateRequest) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        // 1. 소유권 검증 로직 호출 (HUB_ADMIN, COMPANY_ADMIN 모두 처리)
        validateCompanyUpdateAccess(company);

        // 이름이 바뀌었을 때 그 이름이 이미 사용중이라면 예외 (이전 허브 서비스 로직 참고)
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

        // 1. MASTER_ADMIN: 무조건 통과
        if ("MASTER_ADMIN".equals(role)) {
            return;
        }

        // 2. HUB_ADMIN: 해당 업체의 Hub ID가 자신의 Hub ID와 일치하는 경우만 통과
        if ("HUB_ADMIN".equals(role)) {
            if (Objects.equals(company.getHubId(), userHubId)) {
                return;
            }
        }

        // 위 모든 조건에 해당하지 않으면 접근 거부
        throw AppException.of(CompanyErrorCode.ACCESS_DENIED);
    }


    @Override
    @Transactional
    public void deleteCompany(UUID companyId) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> AppException.of(CompanyErrorCode.COMPANY_NOT_FOUND));

        // 1. 소유권 검증 로직 호출 (HUB_ADMIN 소속 허브 검증)
        validateCompanyDeleteAccess(company);

        Long deletedBy = authHeaderExtractor.getUserId(getCurrentRequest());

        if (deletedBy == null) {
            // 게이트웨이에서 헤더가 누락된 경우 등의 예외 처리
            throw AppException.of(CompanyErrorCode.ACCESS_DENIED);
        }

        // 2. 논리적 삭제
        company.delete(deletedBy);
    }

    // COMPANY_ADMIN의 조회 범위 제한을 위한 헬퍼 메서드
    private Pageable filterPageableByRole(Pageable pageable) {
        HttpServletRequest request = getCurrentRequest();
        String role = authHeaderExtractor.getUserRole(request);

        if ("COMPANY_ADMIN".equals(role)) {
            // COMPANY_ADMIN은 전체 조회 시 자신의 업체 데이터만 볼 수 있도록
            // QueryDSL/Criteria 등을 사용하여 필터링해야 하지만,
            // 현재는 Repository 인터페이스만 있으므로,
            // 검색 로직을 통해 해당 사용자의 Company ID만 조회하도록 처리해야 함
            // (이 부분은 Repository 구현에 따라 달라지며, 현재 코드에서는 직접적인 구현이 불가하여
            // Service 내에서 Role에 따른 응답 처리로 대체하거나 Repository 메소드를 변경해야 함)

            // 단순화를 위해 현재는 전체 조회 후 클라이언트에서 필터링한다고 가정하거나
            // COMPANY_ADMIN이 전체 조회를 요청하면 에러를 발생시키는 방어 로직을 고려해야 함.
            // 기획서에는 '다른 업체의 읽기와 검색만 가능'이라고 되어 있어, 이 부분은 예외 처리하지 않고
            // 다음 메서드에서 조회 범위를 조정하는 것으로 대체합니다.
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
            // COMPANY_ADMIN은 자신의 업체만 수정 가능하고, 다른 업체는 읽기/검색만 가능함.
            // 즉, 전체 검색은 허용하되, 특정 업체에 대한 단건/수정/삭제 시에만 제한을 두는 것으로 해석합니다.
            // 따라서 전체 조회 로직은 그대로 유지합니다.
        }

        Page<Company> companies = (keyword != null && !keyword.isEmpty())
                ? companyRepository.searchCompanies(keyword, pageable)
                : companyRepository.findAllActive(pageable);

        return companies.map(CompanyResponse::new);
    }
}