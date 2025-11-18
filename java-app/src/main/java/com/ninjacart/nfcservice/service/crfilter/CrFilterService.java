package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.CrDataAndConfigDto;
import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.configuration.DynamicPropertyHelper;
import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.ProviderConfig;
import com.ninjacart.nfcservice.dtos.ProviderConfigurationDto;
import com.ninjacart.nfcservice.dtos.ProviderIdDto;
import com.ninjacart.nfcservice.dtos.UserStoreSearchResponseDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.enums.ProviderConfigEnum;
import com.ninjacart.nfcservice.service.ElasticSearchService;
import com.ninjacart.nfcservice.service.ProviderConfigurationService;
import com.ninjacart.nfcservice.service.WorkflowRestService;
import com.ninjacart.nfcservice.utils.CommonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrFilterService {

  @Autowired
  private ElasticSearchService elasticSearchService;

  @Autowired
  private ProviderConfigurationService providerConfigurationService;

  @Autowired
  private CrRoundRobinFilter crRoundRobinFilter;

  @Autowired
  private WorkflowRestService workflowRestService;

  @Autowired
  private Set<? extends CrFilter> crFilters;

  @LogExecutionTime
  public Set<CustomerRepresentativeDto> getCustomerRepresentativeDtos(ProviderIdDto providerIdDto,
      RequestDto requestDto) {
    Set<CustomerRepresentativeDto> uniqueCustomeRepresentativeDtos = new HashSet<>();
    if (providerIdDto.getBuyerProviderId().equals(providerIdDto.getSellerProviderId())) {

      CrDataAndConfigDto crDataAndConfigDto = getCrDataAndConfigs(
          providerIdDto.getBuyerProviderId());
      if (Objects.isNull(crDataAndConfigDto) && Objects.isNull(
          crDataAndConfigDto.getUserStoreSearchResponseDto())) {
        return Collections.emptySet();
      }
      CompletableFuture<List<CustomerRepresentativeDto>> cfBuyerCrs = CompletableFuture.supplyAsync(
          () -> getAllCrs(requestDto, crDataAndConfigDto, true));
      CompletableFuture<List<CustomerRepresentativeDto>> cfSellerCrs = CompletableFuture.supplyAsync(
          () -> getAllCrs(requestDto, crDataAndConfigDto, false));
      List<CustomerRepresentativeDto> buyerCRs = null;
      List<CustomerRepresentativeDto> sellerCRs = null;

      try {
        buyerCRs = cfBuyerCrs.get();
        sellerCRs = cfSellerCrs.get();

      } catch (InterruptedException | ExecutionException e) {
        log.error("Error while filtering CRs parallely,{} ", e.getMessage());
        CommonUtils.logAndGetException("Error while filtering CRs parallely.");
      }

      uniqueCustomeRepresentativeDtos.addAll(buyerCRs);
      uniqueCustomeRepresentativeDtos.addAll(sellerCRs);

      return uniqueCustomeRepresentativeDtos;
    } else {
      CompletableFuture<List<CustomerRepresentativeDto>> cfBuyerCrs = CompletableFuture.supplyAsync(
          () -> {
            CrDataAndConfigDto buyerCrDataAndConfigDto = getCrDataAndConfigs(
                providerIdDto.getBuyerProviderId());
            List<CustomerRepresentativeDto> buyerCRs;
            if (Objects.isNull(buyerCrDataAndConfigDto) || Objects.isNull(
                buyerCrDataAndConfigDto.getUserStoreSearchResponseDto())) {
              buyerCRs = Collections.emptyList();
            } else {
              buyerCRs = getAllCrs(requestDto, buyerCrDataAndConfigDto,
                  true);
            }
            return buyerCRs;
          });

      CompletableFuture<List<CustomerRepresentativeDto>> cfSellerCrs = CompletableFuture.supplyAsync(
          () -> {
            CrDataAndConfigDto sellerCrDataAndConfigDto = getCrDataAndConfigs(
                providerIdDto.getSellerProviderId());
            List<CustomerRepresentativeDto> sellerCRs;
            if (Objects.isNull(sellerCrDataAndConfigDto) || Objects.isNull(
                sellerCrDataAndConfigDto.getUserStoreSearchResponseDto())) {
              sellerCRs = Collections.emptyList();
            } else {
              sellerCRs = getAllCrs(requestDto, sellerCrDataAndConfigDto,
                  false);
            }
            return sellerCRs;
          });

      try {
        uniqueCustomeRepresentativeDtos.addAll(cfBuyerCrs.get());
        uniqueCustomeRepresentativeDtos.addAll(cfSellerCrs.get());

      } catch (InterruptedException | ExecutionException e) {
        log.error("Error while filtering CRs parallely,{} ", e.getMessage());
        CommonUtils.logAndGetException("Error while filtering CRs parallely.");
      }

      return uniqueCustomeRepresentativeDtos;
    }
  }

  private CrDataAndConfigDto getCrDataAndConfigs(String providerId) {
    List<String> providerRealmIdentifier = new ArrayList<>();
    UserStoreSearchResponseDto userStoreSearchResponseDto = null;

    providerRealmIdentifier.add(
        DynamicPropertyHelper.getValue(providerId, ""));

    ProviderConfig providerConfig = getFilterConfigs(providerId);
    List<String> filterConfigs = providerConfig.getFilterConfigs();
    List<String> assignmentConfigs = providerConfig.getAssignmentConfigs();
    if (Objects.nonNull(assignmentConfigs) && !assignmentConfigs.contains(
        ProviderConfigEnum.MANUAL.toString())) {
      userStoreSearchResponseDto = workflowRestService.userStoreAdvancedSearch(
          getWorkflowQuery(providerRealmIdentifier));
    }
    log.debug("Provider configuration----------:,{}", providerConfig);

    CrDataAndConfigDto crDataAndConfigDto = CrDataAndConfigDto.builder()
        .filterConfigs(filterConfigs).assignmentConfigs(assignmentConfigs).
        userStoreSearchResponseDto(userStoreSearchResponseDto).build();
    return crDataAndConfigDto;
  }

  private ProviderConfig getFilterConfigs(String providerId) {

    List<String> filterConfigs = new ArrayList<>();
    List<String> assignmentConfigs = new ArrayList<>();
    ProviderConfigurationDto providerConfigurationDto = providerConfigurationService.fetchProviderConfig(
        providerId);
    if (Objects.isNull(providerConfigurationDto)) {
      CommonUtils.logAndGetException("No provider configuration present for " + providerId);
    }

    filterConfigs.add(providerConfigurationDto.getCategoryFilter());
    filterConfigs.add(providerConfigurationDto.getLocationFilter());
    assignmentConfigs.add(providerConfigurationDto.getAssignmentTechnique());
    ProviderConfig providerConfig = ProviderConfig.builder().filterConfigs(filterConfigs)
        .assignmentConfigs(assignmentConfigs).build();
    return providerConfig;
  }

  @LogExecutionTime
  public List<CustomerRepresentativeDto> getAllCrs(RequestDto requestDto,
      CrDataAndConfigDto crDataAndConfigDto,
      boolean isBuyerConfig) {

    List<String> filterConfigs = crDataAndConfigDto.getFilterConfigs();
    List<String> assignmentConfigs = crDataAndConfigDto.getAssignmentConfigs();
    UserStoreSearchResponseDto userStoreSearchResponseDto = crDataAndConfigDto.getUserStoreSearchResponseDto();
    if (Objects.nonNull(userStoreSearchResponseDto) && !userStoreSearchResponseDto.getData()
        .getData()
        .isEmpty()) {
      List<CustomerRepresentativeDto> customerRepresentativeDtos = userStoreSearchResponseDto.getData()
          .getData().stream().map(each -> each.getCustomerRepresentativeDto())
          .collect(Collectors.toList());
      log.debug("All CustomerRepresentative by realmIdentifier of Bayer,{}",
          customerRepresentativeDtos);
      List<CustomerRepresentativeDto> filteredCustomerRepresentativeDtos = getFilteredCrs(
          customerRepresentativeDtos, requestDto, isBuyerConfig, filterConfigs);
      log.debug("Filtered CustomerRepresentatives by config,{}",
          filteredCustomerRepresentativeDtos);
      if (assignmentConfigs.contains(ProviderConfigEnum.ROUNDROBIN.toString())
          && filteredCustomerRepresentativeDtos.size() > 1) {
        filteredCustomerRepresentativeDtos = crRoundRobinFilter.filter(
            filteredCustomerRepresentativeDtos,
            requestDto, isBuyerConfig, filterConfigs);
        log.debug("Customer Representatives by RoundRobin,{}", filteredCustomerRepresentativeDtos);
      }

      return filteredCustomerRepresentativeDtos;
    }
    return Collections.emptyList();

  }


  public List<CustomerRepresentativeDto> getFilteredCrs(
      List<CustomerRepresentativeDto> customerRepresentativeDtos, RequestDto requestDto,
      boolean isBuyerConfig, List<String> filterConfigs) {

    if (customerRepresentativeDtos.isEmpty()) {
      return new ArrayList<>();
    }

    CrFilterExecutor crFilterExecutor = new CrFilterExecutor((Set<CrFilter>) crFilters,
        isBuyerConfig,
        customerRepresentativeDtos, filterConfigs);
    return crFilterExecutor.filterCr(requestDto);
  }


  private String getWorkflowQuery(List<String> providerRealmIdentifier) {
    String commaSeperatedRealmIdentifiers = providerRealmIdentifier.stream()
        .collect(Collectors.joining("\",\"", "\"", "\""));
    return "{\n"
        + "    \"input\": {\n"
        + "        \"query\": {\n"
        + "            \"size\": 100,\n"
        + "            \"query\": {\n"
        + "                \"bool\": {\n"
        + "                    \"must\": [\n"
        + "                        {\n"
        + "                            \"terms\": {\n"
        + "                                \"RealmRelations.realmIdentifier.keyword\": [\n"
        + commaSeperatedRealmIdentifiers
        + "                                ]\n"
        + "                            }\n"
        + "                        },{\n"
        + "                            \"term\": {\n"
        + "                                \"RealmRelations.role.keyword\": \"CUSTOMERREPRESENTATIVE\"\n"
        + "                            }\n"
        + "                        }\n"
        + "                    ]\n"
        + "                }\n"
        + "            }\n"
        + "        }\n"
        + "    }\n"
        + "}";
  }
}