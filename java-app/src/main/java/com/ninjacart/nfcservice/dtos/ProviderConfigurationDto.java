package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfigurationDto {

  @JsonProperty("provider_id")
  @NotEmpty
  private String providerId;

  @JsonProperty("location_filter")
  private String locationFilter;

  @JsonProperty("category_filter")
  private String categoryFilter;

  @JsonProperty("assignment_technique")
  private String assignmentTechnique;

  @JsonProperty("fallback_agent_details")
  private String fallbackAgentDetails;

  @JsonProperty("dispute_api_url")
  private String disputeApiUrl;

  @JsonProperty("dispute_api_auth_token")
  private String disputeApiAuthToken;
}
