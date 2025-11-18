package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service

public class CrStateFilter extends CrFilter {

  private static final String STATE_CONFIG_NAME = "STATE";

  @Override
  public List<CustomerRepresentativeDto> filter(
      List<CustomerRepresentativeDto> customerRepresentativeDtos, RequestDto requestDto,
      boolean isBuyerConfig, List<String> filterConfigs) {

    if (!shouldRun(filterConfigs)) {
      return customerRepresentativeDtos;
    }

    Optional<String> buyerStateOptional = Optional.ofNullable(
        requestDto.getMessage().getRequest().getBuyer().getAddress().getState());
    Optional<String> sellerStateOptional = Optional.ofNullable(
        requestDto.getMessage().getRequest().getSeller().getAddress().getState());
    if (!buyerStateOptional.isPresent() && isBuyerConfig) {
      return customerRepresentativeDtos;
    }
    if (!sellerStateOptional.isPresent() && !isBuyerConfig) {
      return customerRepresentativeDtos;
    }
    List<CustomerRepresentativeDto> stateFilteredRespresentatives;
    if (isBuyerConfig) {
      stateFilteredRespresentatives = getFilteredCrs(customerRepresentativeDtos,
          buyerStateOptional);
    } else {
      stateFilteredRespresentatives = getFilteredCrs(customerRepresentativeDtos,
          sellerStateOptional);
    }

    customerRepresentativeDtos = stateFilteredRespresentatives;
    return customerRepresentativeDtos;
  }

  private List<CustomerRepresentativeDto> getFilteredCrs(
      List<CustomerRepresentativeDto> customerRepresentativeDtos,
      Optional<String> buyerSellerStateOptional) {
    return customerRepresentativeDtos.stream()
        .filter(each -> each.getAdditionalDetails()
            .stream().anyMatch(ref -> ref.getRefType().equals("state") && ref.getRefValue()
                .equalsIgnoreCase(
                    buyerSellerStateOptional.get())))
        .collect(
            Collectors.toList());
  }

  @Override
  public boolean shouldRun(List<String> configEnums) {
    if (configEnums.isEmpty()) {
      return true;
    }
    return configEnums.contains(STATE_CONFIG_NAME);
  }
}

