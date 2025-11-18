package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityMemberDto {

    @JsonProperty("user_info")
    private SellerDto memberDto;
    @JsonProperty("community_id")
    private String communityId;
    @JsonProperty("initiated_by")
    private InitiatedByDto initiatedByDto;


}
