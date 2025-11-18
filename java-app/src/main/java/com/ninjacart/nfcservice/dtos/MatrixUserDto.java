package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class MatrixUserDto {

  private String matrixUserId;
  private String providerUserId;
  private Integer nfcId;
  private String providerId;
  private String matrixUserNameFormat;
  private String matrixToken;
  private String matrixDeviceId;
  private String role;
  private String userName;
  private String password;
  private List<String> category;
  private boolean
      matrixChatAdmin; // will have special access to chat server. not advisable to make other users
                       // as admin
}
