package com.ninjacart.nfcservice.dtos.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NfcUserId {
    @JsonProperty("nfc_user_id")
    private String nfcUserId;
}
