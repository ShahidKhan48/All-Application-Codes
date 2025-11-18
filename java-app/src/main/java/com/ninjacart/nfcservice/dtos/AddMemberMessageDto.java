package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.firebase.database.annotations.NotNull;
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
public class AddMemberMessageDto {
    @NotEmpty
    @JsonProperty("room_id")
    private String roomId;
    @NotNull
    @JsonProperty("nfc_user_id")
    private Integer nfcUserId;
    @NotEmpty
    private String name;
    @JsonProperty("contact_number")
    private String contactNumber;
    private String email;
    private Integer Id;
    @NotEmpty
    @JsonProperty("external_reference_id")
    private String externalReferenceId;
    @NotNull
    @JsonProperty("party_type")
    private Integer partyType;
}
