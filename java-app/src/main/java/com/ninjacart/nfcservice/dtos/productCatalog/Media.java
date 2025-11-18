package com.ninjacart.nfcservice.dtos.productCatalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Media {

    private String id;
    private String type;
    private String visibility;
    private List<MediaDetails> media;
}