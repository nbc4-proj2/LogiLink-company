package com.logilink.company.domain.company.model.entity;


import com.sparta.logilinkcommon.common.BaseTimeEntity;
import com.sparta.logilinkcommon.common.constants.CompanyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "p_companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Company extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "company_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CompanyType type;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "hub_id", nullable = false, columnDefinition = "UUID")
    private UUID hubId;

    public void update(String name, String address, Double latitude, Double longitude, CompanyType type) {
        if (name != null) this.name = name;
        if (address != null) this.address = address;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (type != null) this.type = type;
    }

    public void delete(Long userId) {
        softDelete(userId);
    }

}
