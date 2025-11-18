package com.ninjacart.nfcservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="admin")
public class Admin {
    @Id
    private int id;

    @Column(name = "provider_id")
    private String providerId;
    @Column(name = "trade_type")
    private String tradeType;
    @Column(name ="user_id")
    private String userId;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "created_at")
    private Date CreatedAt;
    @Column(name = "updated_at")
    private Date UpdatedAt;
    @Column(name = "created_by")
    private int createdBy;
    @Column(name = "updated_by")
    private int updatedBy;
    @Column(name = "Active")
    private boolean active;

}
