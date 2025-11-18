package com.ninjacart.nfcservice.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ninjacart.nfcservice.dtos.request.Context;
import com.ninjacart.nfcservice.dtos.request.Message;
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
public class OnboardRequestDto {

    @NotNull
    @Valid
    private Context context;
    @NotNull
    @Valid
    private OnboardMessageDto message;
}
