package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service

public class CrFacilityFilter extends CrFilter {

  private static final String FACILITY_CONFIG_NAME = "FACILITY";

  @Override
  public List<CustomerRepresentativeDto> filter(
      List<CustomerRepresentativeDto> customerRepresentativeDtos, RequestDto requestDto,
      boolean isBuyerConfig, List<String> filterConfigs) {
    if (!shouldRun(filterConfigs)) {
      return customerRepresentativeDtos;
    }

    Optional<String> buyerFacilityOptional = Optional.ofNullable(
        requestDto.getMessage().getRequest().getBuyer().getFacilityId());
    Optional<String> sellerFacilityOptional = Optional.ofNullable(
        requestDto.getMessage().getRequest().getSeller().getFacilityId());

    if (isBuyerConfig && !buyerFacilityOptional.isPresent()) {
      return customerRepresentativeDtos;
    }
    if (!isBuyerConfig && !sellerFacilityOptional.isPresent()) {
      return customerRepresentativeDtos;
    }
    List<CustomerRepresentativeDto> facilityFilteredRespresentatives;
    if (isBuyerConfig) {
      facilityFilteredRespresentatives = getFilteredCrs(customerRepresentativeDtos,
          buyerFacilityOptional);
    } else {
      facilityFilteredRespresentatives = getFilteredCrs(customerRepresentativeDtos,
          sellerFacilityOptional);
    }

    customerRepresentativeDtos = facilityFilteredRespresentatives;
    return customerRepresentativeDtos;
  }

  private List<CustomerRepresentativeDto> getFilteredCrs(
      List<CustomerRepresentativeDto> customerRepresentativeDtos,
      Optional<String> buyerSellerFacilityOptional) {
    return customerRepresentativeDtos.stream()
        .filter(each -> each.getAdditionalDetails()
            .stream().anyMatch(ref -> ref.getRefType().equals("facilityId") && ref.getRefValue()
                .equals(
                    buyerSellerFacilityOptional.get())))
        .collect(
            Collectors.toList());
  }

  @Override
  public boolean shouldRun(List<String> configEnums) {

    if (configEnums.isEmpty()) {
      return true;
    }
    return configEnums.contains(FACILITY_CONFIG_NAME);
  }
}

