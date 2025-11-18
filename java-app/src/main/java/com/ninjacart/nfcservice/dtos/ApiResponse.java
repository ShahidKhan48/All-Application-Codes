package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

  private boolean success = true;
  private Integer errorCode;
  private String errorMessage;
  private T data;
  private String message;

  public ApiResponse(T data) {
    this.data = data;
  }

  public ApiResponse(String message, T data) {
    this.message = message;
    this.data = data;
  }
}
