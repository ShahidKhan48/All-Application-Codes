package com.ninjacart.nfcservice.exception.errorcode;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum ErrorCode {

    CLIENT_TIMEOUT(408, "Request to client timed out"),
    SERVER_UNREACHABLE(403, "Destination server unreachable"),
    CLIENT_ERROR(400, "Client error during request"),
    EMPTY_RESPONSE(204, "Empty response from client"),
    UNKNOWN_ERROR(500, "Unexpected error occurred"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NO_USER_ID_FOUND(404, "User Id not found"),
    RESOURCE_NOT_FOUND_EXCEPTION(404, "Resource not found"),
    CLONE_NOT_SUPPORTED(500, "clone not supported"),
    TIME_OUT(408, "Time out exception"),
    LOCK_ACQUISITION_EXCEPTION(500, "Cannot acquire lock with Redis"),
    ERROR_MSG(400,"%s"),
    BAD_REQUEST(400, "BadRequest");

    private int code;
    private String errorMessage;

    ErrorCode(int code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
