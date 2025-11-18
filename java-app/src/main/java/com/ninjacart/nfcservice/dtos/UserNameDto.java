package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class UserNameDto {

    private String buyerUserName;
    private String sellerUserName;
    private String buyerMatrixToken;
    private String sellerMatrixToken;
    private String buyerDeviceId;
    private String sellerDeviceId;
    private String sellerUserId;
    private String buyerUserId;
}
