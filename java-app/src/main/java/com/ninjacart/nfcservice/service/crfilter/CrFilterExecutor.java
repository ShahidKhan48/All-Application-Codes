package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import java.util.List;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@NoArgsConstructor
public class CrFilterExecutor {

  private Set<CrFilter> crFilters;
  private boolean isBuyerConfig;
  List<CustomerRepresentativeDto> customerRepresentativeDtos;
  private List<String> filterConfigs;

  public CrFilterExecutor(Set<CrFilter> crFilters, boolean isBuyerConfig,
      List<CustomerRepresentativeDto> customerRepresentativeDtos,List<String> filterConfigs) {
    this.crFilters = crFilters;
    this.isBuyerConfig = isBuyerConfig;
    this.customerRepresentativeDtos = customerRepresentativeDtos;
    this.filterConfigs=filterConfigs;
  }

  public List<CustomerRepresentativeDto> filterCr(RequestDto requestDto) {
    for (CrFilter crFilter : crFilters) {
      log.debug("Filters loop--------------------------------");
      log.debug("filters obj--------------------------,{}",crFilters);
      customerRepresentativeDtos = crFilter.filter(customerRepresentativeDtos, requestDto,
          isBuyerConfig,filterConfigs);
    }
    log.info("Filtering------------------------------,{}  ", customerRepresentativeDtos);
    return customerRepresentativeDtos;

  }
}