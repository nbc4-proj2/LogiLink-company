package com.logilink.company.common.exception;

import com.logilink.company.domain.company.model.entity.Company;
import com.sparta.logilinkcommon.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CompanyErrorCode implements ErrorCode {

    COMPANY_NOT_FOUND("COMPANY0001", "해당 업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMPANY_NAME_DUPLICATE("COMPANY0002", "이미 사용 중인 업체 이름입니다.", HttpStatus.CONFLICT),
    HUB_NOT_FOUND("COMPANY0003", "해당 허브를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    CompanyErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
