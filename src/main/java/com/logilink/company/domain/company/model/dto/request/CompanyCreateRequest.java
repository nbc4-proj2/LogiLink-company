package com.logilink.company.domain.company.model.dto.request;

import com.logilink.company.domain.company.model.entity.Company;
import com.sparta.logilinkcommon.common.constants.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CompanyCreateRequest {

    @NotBlank(message = "업체 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "업체 주소는 필수입니다.")
    private String address;

    private Double latitude;
    private Double longitude;

    @NotNull(message = "업체 유형을 입력하세요.")
    private CompanyType type;

    @NotNull(message = "허브 ID는 필수입니다.")
    private UUID hubId;

    @NotNull(message = "유저 ID는 필수입니다.")
    private Long userId;

    public Company toEntity() {
        return Company.builder()
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .type(type)
                .hubId(hubId)
                .userId(userId).build();
    }
}
