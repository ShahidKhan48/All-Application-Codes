package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandRequestDto {

    @NotEmpty
    @JsonProperty("reference_id")
    private String referenceId;
    @NotEmpty
    @JsonProperty("command_event_id")
    private String commandEventId;
    @NotEmpty
    @JsonProperty("room_id")
    private String roomId;
    @NotEmpty
    private String status;
    @JsonProperty("command_type")
    private CommandTypeEnum commandType;


}
