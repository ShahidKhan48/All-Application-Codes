package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.configuration.DynamicPropertyHelper;
import com.ninjacart.nfcservice.dtos.NonceDto;
import com.ninjacart.nfcservice.dtos.OnboardDto;
import com.ninjacart.nfcservice.dtos.OnboardResponseDto;
import com.ninjacart.nfcservice.entity.Users;
import com.ninjacart.nfcservice.repository.UsersRepository;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsersService {

  @Autowired private UsersRepository usersRepository;

  @Autowired private MatrixRestService matrixRestService;

  private static final String MATRIX_PREFIX = ":95.216.170.148";
  private static final String MATRIX_USER_ID_PREFIX = "@";
  private static final String MATRIX_PREFIX_CONFIG_KEY = "matrix.user.prefix";
  private static final String MATRIX_SHARED_SECRET = "ZDHB3WMSMdxzdWpTEEbp8t4q/jex2olncbRDfTMs";
  private static final String MATRIX_SHARED_SECRET_CONFIG_KEY = "matrix.shared.secret";
  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
  private static final String ADMIN = "admin";
  private static final String NOT_ADMIN = "notadmin";

  public List<Users> findByUserIds(List<String> matrixUserIds) {
    return usersRepository.findAllByNameInAndDeactivated(matrixUserIds, 0);
  }

  private static String getMatrixPrefixId() {

    return DynamicPropertyHelper.getValue(MATRIX_PREFIX_CONFIG_KEY, MATRIX_PREFIX);
  }

  public Map<String, String> formatNamesToMatrixUserIds(List<String> userNames) {
    String matrixUserPrefix = getMatrixPrefixId();
    return userNames.stream()
        .collect(
            Collectors.toMap(
                e -> e,
                e -> {
                  StringBuilder formattedName =
                      new StringBuilder(MATRIX_USER_ID_PREFIX).append(e).append(matrixUserPrefix);
                  return formattedName.toString();
                },
                (a, b) -> b));
  }

  public OnboardResponseDto onboardUser(OnboardDto onboardDto)
      throws NoSuchAlgorithmException, InvalidKeyException {
    NonceDto nonceDto = matrixRestService.getNonce();

    if (nonceDto == null || StringUtils.isEmpty(nonceDto.getNonce())) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }

    onboardDto.setNonce(nonceDto.getNonce());
    String sharedSecret =
        DynamicPropertyHelper.getValue(MATRIX_SHARED_SECRET_CONFIG_KEY, MATRIX_SHARED_SECRET);

    log.debug("onboardDto : {}", onboardDto);

    String hmacDigest = calculateHmacForOnboardDto(onboardDto, sharedSecret);
    log.debug("hmacDigest : {}", hmacDigest);

    onboardDto.setMac(hmacDigest);

    return matrixRestService.onboardUser(onboardDto);
  }

  private static String toHexString(byte[] bytes) {
    Formatter formatter = new Formatter();

    for (byte b : bytes) {
      formatter.format("%02x", b);
    }

    return formatter.toString();
  }

  public static String calculateHmacForOnboardDto(OnboardDto onboardDto, String key)
      throws NoSuchAlgorithmException, InvalidKeyException {

    SecretKeySpec signingKey =
        new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA1_ALGORITHM);
    Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
    mac.init(signingKey);
    String x =
        String.format(
            "%s\0%s\0%s\0%s",
            onboardDto.getNonce(),
            onboardDto.getUsername(),
            onboardDto.getPassword(),
            onboardDto.isAdmin() ? ADMIN : NOT_ADMIN);
    return toHexString(mac.doFinal(x.getBytes(StandardCharsets.UTF_8)));
  }
}
