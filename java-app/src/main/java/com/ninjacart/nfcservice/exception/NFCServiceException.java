package com.ninjacart.nfcservice.exception;

import com.ninjacart.nfcservice.exception.errorcode.ErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/** Nfc service exception */
@Data
@NoArgsConstructor
public class NFCServiceException extends RuntimeException {

  private Integer errorCode;
  private String errorMessage;
  private HttpStatus httpStatus;

  public NFCServiceException(ErrorCode errorCode) {
    super(errorCode.getErrorMessage());
    this.errorCode = errorCode.getCode();
    this.errorMessage = errorCode.getErrorMessage();
  }

  public NFCServiceException(ErrorCode errorCode, String errorMessage) {
    super(errorMessage);
    this.errorCode = errorCode.getCode();
    this.errorMessage = errorMessage;
  }

  public NFCServiceException(String errorMessage) {
    super(errorMessage);
    this.errorCode = ErrorCode.BAD_REQUEST.getCode();
    this.errorMessage = errorMessage;
  }

  public NFCServiceException(String errorMessage, HttpStatus httpStatus) {
    super(errorMessage);
    this.httpStatus = httpStatus;
    this.errorCode = ErrorCode.BAD_REQUEST.getCode();
    this.errorMessage = errorMessage;
  }

  public NFCServiceException(String errorMessage, Exception e) {
    super(e);
    this.errorCode = ErrorCode.BAD_REQUEST.getCode();
    this.errorMessage = errorMessage;
  }

  public NFCServiceException(Integer customErrorCode, String errorMessage) {
    super(errorMessage);
    this.errorCode = customErrorCode;
    this.errorMessage = errorMessage;
  }

  public NFCServiceException(Exception e, String errorMessage) {
    super(e);
    this.errorMessage = errorMessage;
  }

  public NFCServiceException(Exception e) {
    super(e);
    this.errorMessage = e.getMessage();
  }
}
