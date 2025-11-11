package com.logilink.company.domain.company.model.dto.response;

import com.logilink.company.domain.company.model.entity.Company;
import com.sparta.logilinkcommon.common.constants.CompanyType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CompanyResponse {

    private UUID id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private CompanyType type;
    private Long userId;
    private UUID hubId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CompanyResponse(Company company) {
        this.id = company.getId();
        this.name = company.getName();
        this.address = company.getAddress();
        this.latitude = company.getLatitude();
        this.longitude = company.getLongitude();
        this.type = company.getType();
        this.userId = company.getUserId();
        this.hubId = company.getHubId();
        this.createdAt = company.getCreatedAt();
        this.updatedAt = company.getUpdatedAt();
    }
}
