package com.ninjacart.nfcservice.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Context {
    @NotEmpty
    private String action;
    @NotEmpty
    @JsonProperty("transaction_id")
    private String transactionId;
    private String timestamp;
    @NotEmpty
    @JsonProperty("provider_id")
    private String providerId;
    @JsonProperty("your_role")
    private String yourRole;
    @JsonProperty("provider_user_id")
    private String providerUserId;
    @JsonProperty("nfc_user_id")
    private String nfcUserId;
}
