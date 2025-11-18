package com.ninjacart.nfcservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Users {

    @Id
    private String name;
    @JsonProperty("password_hash")
    private String passwordHash;
    @JsonProperty("creation_ts")
    private BigInteger creationTs;
    private int admin;
    @JsonProperty("upgrade_ts")
    private BigInteger upgradeTs;
    @JsonProperty("is_guest")
    private int isGuest;
    private int deactivated;
    private boolean approved;
    @JsonProperty("shadow_banned")
    private boolean shadowBanned;



}
