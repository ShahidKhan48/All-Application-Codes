package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class  CustomerRepresentativeDto {
  private String role;
  private String name;
  private List<AdditionalDetail> additionalDetails;
  private int userId;
  private String realmIdentifier;
  private String osCreatedAt;
  private String osUpdatedAt;
  private String id;
}
