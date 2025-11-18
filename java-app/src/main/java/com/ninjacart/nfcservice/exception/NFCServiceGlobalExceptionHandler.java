package com.ninjacart.nfcservice.exception;

import com.ninjacart.nfcservice.dtos.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.ninjacart.nfcservice.exception.errorcode.ErrorCode.BAD_REQUEST;
import static com.ninjacart.nfcservice.exception.errorcode.ErrorCode.INTERNAL_SERVER_ERROR;

@ControllerAdvice(basePackages = {"com.ninjacart"})
@Slf4j
public class NFCServiceGlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  public @ResponseBody ApiResponse handleException(Exception ex) {
    String errorMsg =
        INTERNAL_SERVER_ERROR.getErrorMessage()
            + ":"
            + (ex.getLocalizedMessage() == null ? "Error" : ex.getLocalizedMessage());
    ApiResponse apiOutput = new ApiResponse();
    apiOutput.setSuccess(false);
    apiOutput.setErrorCode(INTERNAL_SERVER_ERROR.getCode());
    apiOutput.setErrorMessage(errorMsg);
    log.error("Exception occurred : {} {}", ex, ex.getStackTrace(), ex);
    return apiOutput;
  }

  @ExceptionHandler(NFCServiceException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public @ResponseBody ApiResponse handleException(NFCServiceException ex) {
    String errorMsg = ex.getLocalizedMessage() == null ? "Error" : ex.getLocalizedMessage();
    ApiResponse apiOutput = new ApiResponse();
    apiOutput.setSuccess(false);
    apiOutput.setErrorCode(BAD_REQUEST.getCode());
    apiOutput.setErrorMessage(errorMsg);
    log.error("Exception occurred : {} {}", ex, ex.getStackTrace(), ex);
    return apiOutput;
  }
}
