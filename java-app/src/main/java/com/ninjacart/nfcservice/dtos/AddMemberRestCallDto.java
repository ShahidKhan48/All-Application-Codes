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
public class AddMemberRestCallDto {

    private String externalReferenceId;
    private Integer freeFlowEntityId;
    private Integer userId;
    private Integer partyId;
    private String name;
    private Integer partyType;
    private String contactNumber;
    private String email;
    private boolean deleted;
}
