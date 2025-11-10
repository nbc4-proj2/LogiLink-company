package com.logilink.company.domain.company.model.dto.request;

import com.logilink.company.domain.company.model.entity.Company;
import com.sparta.logilinkcommon.common.constants.CompanyType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyUpdateRequest {

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private CompanyType type;

    public Company toEntity() {
        return Company.builder()
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .type(type).build();
    }
}
