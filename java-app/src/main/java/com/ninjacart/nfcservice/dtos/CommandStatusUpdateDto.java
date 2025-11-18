package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandStatusUpdateDto {

    @Valid
    @NotNull
    @JsonProperty("initiated_by")
    private InitiatedByDto initiatedByDto;

    @Valid @NotNull private CommandStatusRequestObjectDto request;

}
