package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunityEntityDto {
    private String realmId;
    private String userId;
    private Boolean partyCommunity;
    private String createdBy;
    private List<MembersObject> members;
    private String id;
    private String type;
    private String chatRoomId;
    private CommunityDescriptorDto descriptor;
    private String communityType;
    private String createdAt;
}
