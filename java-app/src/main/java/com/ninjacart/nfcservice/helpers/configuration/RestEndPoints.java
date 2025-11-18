package com.ninjacart.nfcservice.helpers.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class RestEndPoints {
    @Value("${baseUrls.matrixUrl}")
    private String matrixBaseUrl;
    @Value("${baseUrls.gemsFetchUrl]")
    private String gemsBaseUrl;

}
