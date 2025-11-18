package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminResponseDto {

  private boolean admin;

  @JsonProperty("nfc_user_id")
  private Integer nfcUserId;

  @JsonProperty("trade_type")
  private String tradeType;

  private boolean onboarded;
}
