package com.ninjacart.nfcservice.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.elasticSearch.Bool;
import com.ninjacart.nfcservice.enums.RequestType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchMessageDto {

    private List<SearchParamDto> search;

    @JsonProperty("request_type")
    private RequestType requestType;
    private Boolean federated;

}
