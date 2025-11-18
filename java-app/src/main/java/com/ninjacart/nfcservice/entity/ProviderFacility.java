package com.ninjacart.nfcservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "provider_facility")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderFacility {
    @Id
    private Integer id;
    @Column(name = "facility_id")
    private String facilityId;
    @Column(name = "facility_name")
    private String facilityName;
    @Column(name = "provider_id")
    private String providerId;
    @Column(name = "city_name")
    private String cityName;

}
