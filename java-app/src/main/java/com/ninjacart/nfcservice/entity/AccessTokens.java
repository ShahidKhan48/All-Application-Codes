package com.ninjacart.nfcservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigInteger;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "access_tokens")
public class AccessTokens {

    @Id
    private String id;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("token")
    private String token;
    @JsonProperty("device_id")
    private String deviceId;
}
