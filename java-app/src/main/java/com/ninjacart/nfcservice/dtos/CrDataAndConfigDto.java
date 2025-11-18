package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ninjacart.nfcservice.dtos.UserStoreSearchResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrDataAndConfigDto {

  private UserStoreSearchResponseDto userStoreSearchResponseDto;
  private List<String> filterConfigs;
  private List<String> assignmentConfigs;
}
