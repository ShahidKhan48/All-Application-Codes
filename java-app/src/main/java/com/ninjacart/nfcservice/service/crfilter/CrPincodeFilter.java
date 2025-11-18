package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CrPincodeFilter extends CrFilter {

  private static final String PINCODE_CONFIG_NAME = "PINCODE";

  @Override
  public List<CustomerRepresentativeDto> filter(
      List<CustomerRepresentativeDto> customerRepresentativeDtos, RequestDto requestDto,
      boolean isBuyerConfig, List<String> filterConfigs) {

    if (!shouldRun(filterConfigs)) {
      return customerRepresentativeDtos;
    }

    Optional<String> buyerPincodeOptional = Optional.ofNullable(
        requestDto.getMessage().getRequest().getBuyer().getAddress().getAreaCode());
    Optional<String> sellerPincodeOptional = Optional.ofNullable(
        requestDto.getMessage().getRequest().getSeller().getAddress().getAreaCode());
    if (isBuyerConfig && !buyerPincodeOptional.isPresent()) {
      return customerRepresentativeDtos;
    }
    if (!isBuyerConfig && !sellerPincodeOptional.isPresent()) {
      return customerRepresentativeDtos;
    }
    List<CustomerRepresentativeDto> pincodeFilteredRespresentatives;
    if (isBuyerConfig) {
      pincodeFilteredRespresentatives = getFilteredCrs(customerRepresentativeDtos,
          buyerPincodeOptional);
    } else {
      pincodeFilteredRespresentatives = getFilteredCrs(customerRepresentativeDtos,
          sellerPincodeOptional);
    }

    customerRepresentativeDtos = pincodeFilteredRespresentatives;
    return customerRepresentativeDtos;
  }

  private List<CustomerRepresentativeDto> getFilteredCrs(
      List<CustomerRepresentativeDto> customerRepresentativeDtos,
      Optional<String> buyerSellerPincodeOptional) {
    return customerRepresentativeDtos.stream()
        .filter(each -> each.getAdditionalDetails()
            .stream().anyMatch(ref -> ref.getRefType().equals("pincode") && ref.getRefValue()
                .equals(
                    buyerSellerPincodeOptional.get())))
        .collect(
            Collectors.toList());
  }

  @Override
  public boolean shouldRun(List<String> configEnums) {
    if (configEnums.isEmpty()) {
      return true;
    }
    return configEnums.contains(PINCODE_CONFIG_NAME);
  }
}
