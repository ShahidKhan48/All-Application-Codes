package com.ninjacart.nfcservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "provider_user")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "provider_id")
    private String providerId;
    @Column(name = "provider_user_id")
    private String providerUserId;
    @Column(name = "chat_user_id")
    private String chatUserId;
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "user_name")
    private String userName;
    private String category;
    @Column(name = "sub_category")
    private String subCategory;
    private String role;
    private String password;
    private String token;
    @Column(name = "fcm_token")
    private String fcmToken;
    private int deleted;
    private int createdBy;
    private int updatedBy;
    private Date createdAt;
    private Date updatedAt;
}
