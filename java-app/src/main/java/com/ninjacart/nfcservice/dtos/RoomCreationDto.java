package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RoomCreationDto {

    private String visibility;   // enum of  ["public", "private"] if its private no one can find this room by search
    private String name;
    private String topic;
    private List<String> invite; // list of matrix userIds
    private String preset; // enum of ["private_chat", "public_chat", "trusted_private_chat"]
    @JsonProperty("creation_content")
    private FederationClass creationContent;
    private Set<String> userIds;
}
