package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service

public class CrCategoryFilter extends CrFilter {

  private static final String CATEGORY_CONFIG_NAME = "CATEGORY";

  @Override
  public List<CustomerRepresentativeDto> filter(
      List<CustomerRepresentativeDto> customerRepresentativeDtos, RequestDto requestDto,
      boolean isBuyerConfig, List<String> filterConfigs) {

    if (!shouldRun(filterConfigs)) {
      return customerRepresentativeDtos;
    }

    List<CustomerRepresentativeDto> cityFilteredRespresentatives = customerRepresentativeDtos.stream()
        .filter(each -> each.getAdditionalDetails().stream().anyMatch(
            ref -> ref.getRefType().equals("category") && isCategoryMatching(ref.getRefValue(),
                requestDto.getMessage().getRequest().getItems())))
        .collect(Collectors.toList());

    customerRepresentativeDtos = cityFilteredRespresentatives;
    return customerRepresentativeDtos;
  }

  @Override
  public boolean shouldRun(List<String> configEnums) {
    if (configEnums.isEmpty()) {
      return true;
    }
    return configEnums.contains(CATEGORY_CONFIG_NAME);
  }

  private boolean isCategoryMatching(String crCategory, List<ItemsDto> itemsDtos) {
    List<String> category = itemsDtos.stream()
        .filter(each -> Objects.nonNull(each.getCategory()) && !each.getCategory().isEmpty())
        .map(each -> each.getCategory().toLowerCase())
        .collect(Collectors.toList());
    List<String> crCategories = Arrays.asList(crCategory.split(",", -1)).stream()
        .map(each -> each.toLowerCase()).collect(
            Collectors.toList());
    for (int i = 0; i < category.size(); i++) {
      if (crCategories.contains(category.get(i))) {
        return true;
      }
    }
    return false;
  }
}

