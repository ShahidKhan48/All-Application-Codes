package com.ninjacart.nfcservice.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientRequestDto {
    @NotEmpty
    @JsonProperty("provider_id")
    private String providerId;
    @NotEmpty
    @JsonProperty("request_type")
    private String requestType;
    @NotNull
    @Valid
    private List<ItemsDto> items;
    @NotNull
    @JsonProperty("source_user")
    @Valid
    private SellerDto sourceUser;
    @NotNull
    @JsonProperty("destination_user")
    @Valid
    private List<SellerDto> destinationUsers;
    private String message;
    @JsonProperty("admin_info")
    private InitiatedByDto adminInfo;
    @JsonProperty("admin_assisted_flow")
    private boolean adminAssistedFlow;
}
