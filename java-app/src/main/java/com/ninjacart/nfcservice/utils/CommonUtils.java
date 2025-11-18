package com.ninjacart.nfcservice.utils;

import com.ninjacart.nfcservice.configuration.DynamicPropertyHelper;
import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import com.ninjacart.nfcservice.exception.NFCServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CommonUtils {

  private static final String MATRIX_PREFIX = ":95.216.170.148";
  private static final String MATRIX_USER_ID_PREFIX = "@";
  private static final String MATRIX_PREFIX_CONFIG_KEY = "matrix.user.prefix";

  /**
   * Generates random number of given length
   *
   * @param length length
   * @return random number
   */
  public static int randomNumberGenerator(int length) {
    int randomPIN =
        (int) (Math.random() * 9 * (Math.pow(10, length - 1)) + Math.pow(10, (length - 1)));
    return randomPIN;
  }

  public static String generateMatrixUniqueTransactionId(int size) {
    return String.format(
        "%s.%s.%s", "m", new Date().getTime(), CommonUtils.randomNumberGenerator(size));
  }

  public static NFCServiceException logAndGetException(String errorMessage, Exception e) {
    log.error(errorMessage, e);
    return new NFCServiceException(errorMessage);
  }

  public static NFCServiceException logAndGetException(String errorMessage) {
    log.error(errorMessage);
    return new NFCServiceException(errorMessage);
  }

  public static Map<String, String> formatNamesToMatrixUserIds(List<String> userNames) {
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

  private static String getMatrixPrefixId() {

    return DynamicPropertyHelper.getValue(MATRIX_PREFIX_CONFIG_KEY, MATRIX_PREFIX);
  }

  public static void checkInitiatedByMandatoryValues(InitiatedByDto initiatedByDto) {
    if(initiatedByDto == null || initiatedByDto.getNfcUserId() == null || StringUtils.isEmpty(initiatedByDto.getUserId()) || StringUtils.isEmpty(initiatedByDto.getAppId()) || initiatedByDto.getRole() == null) {
      throw logAndGetException("Invalid Input");
    }
  }

  public static void checkUserMandatoryValues(SellerDto dto) {
    if(dto == null || dto.getNfcUserId() == null || StringUtils.isEmpty(dto.getUserId()) || StringUtils.isEmpty(dto.getAppId()) || StringUtils.isEmpty(dto.getName())) {
      throw logAndGetException("Invalid Input");
    }
  }

  public static void checkItemsMandatoryValues(List<ItemsDto> itemsDtos) {
   if(CollectionUtils.isEmpty(itemsDtos)) {
     throw logAndGetException("Invalid input");
   }

   for(ItemsDto each: itemsDtos) {
     if(StringUtils.isEmpty(each.getName()) || each.getPrice() == null || each.getQuantity() == null || each.getPrice().getValue() == null || each.getQuantity().getCount() == null) {
       throw logAndGetException("Invalid input");
     }
   }
  }
  public static SimpleDateFormat dateFormatterLong() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    return sdf;
  }
}
