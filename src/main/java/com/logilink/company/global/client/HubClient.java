package com.logilink.company.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * 허브 서비스와 통신하기 위한 Feign 클라이언트
 * - 허브 존재 여부를 확인할 때 사용
 */
@FeignClient(
        name = "hub-service",
        url = "http://localhost:19096" // 허브 서비스의 실제 실행 포트
)
public interface HubClient {

    /**
     * 허브 존재 여부 확인 API 호출
     * @param hubId 확인할 허브의 UUID
     * @return 존재하면 true, 존재하지 않으면 false
     */
    @GetMapping("/api/v1/hubs/{hubId}/exists")
    boolean existsHub(@PathVariable("hubId") UUID hubId);
}