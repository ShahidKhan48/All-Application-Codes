package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrCityFilter extends CrFilter {

  private static final String CITY_CONFIG_NAME = "CITY";

  @Override
  public List<CustomerRepresentativeDto> filter(
      List<CustomerRepresentativeDto> customerRepresentativeDtos, RequestDto requestDto,
      boolean isBuyerConfig, List<String> filterConfigs) {
    log.debug("CR input in filter:---------------,{}",customerRepresentativeDtos);
    log.debug("Buyer config in filter:---------------,{}",isBuyerConfig);

    if (!shouldRun(filterConfigs)) {
      return customerRepresentativeDtos;
    }
    Optional<String> cityOptionalBuyer = Optional.ofNullable(
        requestDto.getMessage().getRequest().getBuyer().getAddress().getCity());
    Optional<String> cityOptionalSeller = Optional.ofNullable(
        requestDto.getMessage().getRequest().getSeller().getAddress().getCity());
    if (!cityOptionalBuyer.isPresent() && isBuyerConfig) {
      return customerRepresentativeDtos;
    }
    if (!cityOptionalSeller.isPresent() && !isBuyerConfig) {
      return customerRepresentativeDtos;
    }

    List<CustomerRepresentativeDto> cityFilteredRespresentatives;
    if (isBuyerConfig) {
      cityFilteredRespresentatives = getFilteredCRs(customerRepresentativeDtos, cityOptionalBuyer);
      log.info("buyer");

    } else {
      cityFilteredRespresentatives = getFilteredCRs(customerRepresentativeDtos, cityOptionalSeller);
      log.info("seller");
    }

    customerRepresentativeDtos = cityFilteredRespresentatives;
    return customerRepresentativeDtos;
  }

  private List<CustomerRepresentativeDto> getFilteredCRs(
      List<CustomerRepresentativeDto> customerRepresentativeDtos,
      Optional<String> buyerSellerOptional) {
    log.info("BuyerSell optional,{}",buyerSellerOptional);
    List<CustomerRepresentativeDto> cityCrs= customerRepresentativeDtos.stream()
        .filter(each -> each.getAdditionalDetails()
            .stream().anyMatch(ref -> ref.getRefType().equals("city") && ref.getRefValue()
                .equalsIgnoreCase(
                    buyerSellerOptional.get())))
        .collect(
            Collectors.toList());
    log.info("City Filter----------------------------,{}",cityCrs);
    return cityCrs;
  }

  @Override
  public boolean shouldRun(List<String> configEnums) {
    if (configEnums.isEmpty()) {
      return true;
    }
    return configEnums.contains(CITY_CONFIG_NAME);
  }
}
