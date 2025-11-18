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
public class EditMessageDto {

    private String body;
    private String msgtype;
    @JsonProperty("m.new_content")
    private MessageBodyDto messageBodyDto;
    @JsonProperty("m.relates_to")
    private MessageRelateDto messageRelateDto;

}
