package com.logilink.company.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Slf4j
@Component("auditorAware")
@RequiredArgsConstructor
public class CustomAuditorAware implements AuditorAware<Long> {

    private final AuthHeaderExtractor authHeaderExtractor;

    @Override
    public Optional<Long> getCurrentAuditor() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return Optional.empty();
            }

            HttpServletRequest request = attributes.getRequest();
            Long userId = authHeaderExtractor.getUserId(request);

            // 게이트웨이가 헤더에 X-User-Id를 안 넣었을 때 (비정상 요청)
            if (userId == null) {
                return Optional.empty();
            }

            return Optional.of(userId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}