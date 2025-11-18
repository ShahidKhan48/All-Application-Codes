package com.ninjacart.nfcservice.dtos.requestGroup;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestGroupDto {

    private Integer id;
    private String providerId;
    private String requestType;
    private String providerUserId;
    private String status;
    private String additionalDetails;
    private List<RequestGroupItems> items;
    private String message;
    private String displayText;
}
