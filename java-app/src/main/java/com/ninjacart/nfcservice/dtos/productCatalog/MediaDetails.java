package com.ninjacart.nfcservice.dtos.productCatalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaDetails {
    private String id;
    private String location;
    private String mediaFormat;
    private String mediaUrl;
    private String visibility;
    private String mediaType;
    private String fileName;

}
