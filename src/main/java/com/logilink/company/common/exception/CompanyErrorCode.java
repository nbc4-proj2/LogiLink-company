package com.logilink.company.common.exception;

import com.logilink.company.domain.company.model.entity.Company;
import com.sparta.logilinkcommon.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CompanyErrorCode implements ErrorCode {

    COMPANY_NOT_FOUND("COMPANY0001", "해당 업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMPANY_NAME_DUPLICATE("COMPANY0002", "이미 사용 중인 업체 이름입니다.", HttpStatus.CONFLICT),
    HUB_NOT_FOUND("COMPANY0003", "해당 허브를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    REQUEST_CONTEXT_NOT_FOUND("COMPANY0004", "요청 컨텍스트를 찾을 수 없습니다. (비정상 접근)", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCESS_DENIED("COMPANY0005", "해당 작업에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    ACCESS_DENIED_HUB_ADMIN_MISMATCH("COMPANY0006", "담당 허브가 아닌 업체의 관리/생성은 불가능합니다.", HttpStatus.FORBIDDEN),
    ACCESS_DENIED_COMPANY_MISMATCH("COMPANY0007", "본인 업체가 아닌 다른 업체의 수정은 불가능합니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    CompanyErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
