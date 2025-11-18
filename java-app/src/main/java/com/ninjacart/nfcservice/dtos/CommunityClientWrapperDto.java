package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ninjacart.nfcservice.dtos.request.Context;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityClientWrapperDto {
    private Context context;
    private CommunityClientDto request;
}
