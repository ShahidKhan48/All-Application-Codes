package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ninjacart.nfcservice.dtos.request.Context;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityClientDto {
    private String type;
    private boolean partyCommunity;
    private String groupName;
    private String imageUrl;
    private InitiatedByDto initiatedBy;
    private SellerDto otherParty;
    private String communityType;
}
