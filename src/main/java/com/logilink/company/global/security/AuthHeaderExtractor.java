package com.logilink.company.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 게이트웨이에서 전달한 인증 헤더(X-User-Id, X-User-Role 등)를
 * 허브 서비스 내부에서 쉽게 꺼내 쓸 수 있도록 도와주는 유틸 클래스.
 */
@Component
public class AuthHeaderExtractor {

    public Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) return null;
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getUserRole(HttpServletRequest request) {
        return request.getHeader("X-User-Role");
    }

    public UUID getHubId(HttpServletRequest request) {
        String hubId = request.getHeader("X-Hub-Id");
        if (hubId == null || hubId.isBlank()) return null;
        try {
            return UUID.fromString(hubId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public UUID getCompanyId(HttpServletRequest request) {
        String companyId = request.getHeader("X-Company-Id");
        if (companyId == null || companyId.isBlank()) return null;
        try {
            return UUID.fromString(companyId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Boolean getIsDeliveryAvailable(HttpServletRequest request) {
        String flag = request.getHeader("X-Is-Delivery-Available");
        return flag != null && Boolean.parseBoolean(flag);
    }
}
