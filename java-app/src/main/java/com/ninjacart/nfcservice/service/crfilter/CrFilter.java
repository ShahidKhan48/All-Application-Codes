package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import java.util.List;

public abstract class CrFilter {

  abstract List<CustomerRepresentativeDto> filter(
      List<CustomerRepresentativeDto> customerRepresentativeDtos,
      RequestDto requestDto, boolean isBuyerConfig, List<String> filterConfigs);

  abstract boolean shouldRun(List<String> configEnums);
}
