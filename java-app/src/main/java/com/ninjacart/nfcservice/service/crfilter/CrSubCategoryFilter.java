package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.SubCategory;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service

@Slf4j
public class CrSubCategoryFilter extends CrFilter {

  private static final String SUB_CATEGORY_NAME = "SUBCATEGORY";

  @Override
  public List<CustomerRepresentativeDto> filter(
      List<CustomerRepresentativeDto> customerRepresentativeDtos, RequestDto requestDto,
      boolean isBuyerConfig, List<String> filterConfigs) {

    log.debug("CR input filter:---------------,{}",customerRepresentativeDtos);
    log.debug("Buyer config filter:---------------,{}",isBuyerConfig);

    if (!shouldRun(filterConfigs)) {
      return customerRepresentativeDtos;
    }
    Optional<List<ItemsDto>> optionalItemsDtos = Optional.ofNullable(
        requestDto.getMessage().getRequest().getItems());
    if (!optionalItemsDtos.isPresent()) {
      return customerRepresentativeDtos;
    }

    List<CustomerRepresentativeDto> cityFilteredRespresentatives = customerRepresentativeDtos.stream()
        .filter(each -> each.getAdditionalDetails().stream().anyMatch(
            ref -> ref.getRefType().equals("subCategory") && isSubCategoryMatching(
                ref.getRefValue(), optionalItemsDtos.get())))
        .collect(Collectors.toList());
  log.info("Categories filtered,{}",cityFilteredRespresentatives);
//    if (cityFilteredRespresentatives.isEmpty()) {
//      return customerRepresentativeDtos;
//    }
    customerRepresentativeDtos = cityFilteredRespresentatives;
    return customerRepresentativeDtos;
  }

  @Override
  public boolean shouldRun(List<String> configEnums) {
    if (configEnums.isEmpty()) {
      return true;
    }
    return configEnums.contains(SUB_CATEGORY_NAME);
  }


  private boolean isSubCategoryMatching(String crCategory, List<ItemsDto> itemsDtos) {

    List<String> subCategoriesBuyerSeller = itemsDtos.stream()
        .map(each -> each.getName().toLowerCase())
        .collect(Collectors.toList());
    List<String> crCategories = Arrays.asList(crCategory.split(",", -1)).stream()
        .map(each -> each.toLowerCase()).collect(
            Collectors.toList());

    for (int i = 0; i < subCategoriesBuyerSeller.size(); i++) {
      if (crCategories.contains(subCategoriesBuyerSeller.get(i))) {
        return true;
      }
    }
    return false;
  }
}
