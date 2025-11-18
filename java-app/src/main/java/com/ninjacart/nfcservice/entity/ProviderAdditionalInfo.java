package com.ninjacart.nfcservice.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="provider_additional_info")
public class ProviderAdditionalInfo {
    @Id
    private Integer id;
    @Column(name = "provider_id")
    private String providerId;
    @Column(name = "key_set")
    private String key;
    @Column(name = "value")
    private String value;
}
