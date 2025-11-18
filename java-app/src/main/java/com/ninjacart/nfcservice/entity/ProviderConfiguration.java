package com.ninjacart.nfcservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Table(name = "provider_configuration")
public class ProviderConfiguration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "provider_id")
  private String providerId;

  @Column(name = "location_filter")
  private String locationFilter;

  @Column(name = "category_filter")
  private String categoryFilter;

  @Column(name = "assignment_technique")
  private String assignmentTechnique;

  @Column(name = "fallback_agent_details")
  private String fallbackAgentDetails;

  @Column(name = "dispute_api_url")
  private String disputeApiUrl;

  @Column(name = "dispute_api_auth_token")
  private String disputeApiAuthToken;

  @Column(name = "created_by")
  private int createdBy;
  @Column(name = "updated_by")
  private int updatedBy;
  private boolean active;
  @Column(name = "created_at")
  private Date createdAt;
  @Column(name = "updated_at")
  private Date updatedAt;
}
