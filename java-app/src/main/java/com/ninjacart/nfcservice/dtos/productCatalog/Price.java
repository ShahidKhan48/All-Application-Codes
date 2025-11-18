package com.ninjacart.nfcservice.dtos.productCatalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price {
    private double minimumPrice;
    private double maximumPrice;
    private String currency;
    private MeasurementUnit measurementUnit;
    private double value;

}
