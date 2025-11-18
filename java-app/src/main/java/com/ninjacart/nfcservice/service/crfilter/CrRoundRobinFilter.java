package com.ninjacart.nfcservice.service.crfilter;

import com.ninjacart.nfcservice.dtos.AdditionalDetail;
import com.ninjacart.nfcservice.dtos.Bucket;
import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;
import com.ninjacart.nfcservice.dtos.ElasticSearchResponseDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.service.ElasticSearchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Addition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrRoundRobinFilter {

  private static final String ROUND_ROBIN_CONFIG_NAME = "ROUNDROBIN";


  @Autowired
  private ElasticSearchService elasticSearchService;


  public List<CustomerRepresentativeDto> filter(
      List<CustomerRepresentativeDto> customerRepresentativeDtos, RequestDto requestDto,
      boolean isBuyerConfig, List<String> filterConfigs) {
    List<String> nfcUserIds = new LinkedList<>();
    for (CustomerRepresentativeDto customerRepresentativeDto : customerRepresentativeDtos) {
      Optional<AdditionalDetail> nfcUserId = customerRepresentativeDto.getAdditionalDetails()
          .stream().filter(each ->
              each.getRefType().equals("nfc_user_id")).findFirst();
      if (nfcUserId.isPresent()) {
        if (Objects.nonNull(nfcUserId.get().getRefValue())) {
          nfcUserIds.add(nfcUserId.get().getRefValue());

        }
      }
    }
    List<String> filteredIds = nfcUserIds.stream()
        .filter(each -> !each.isEmpty() && Objects.nonNull(each))
        .collect(Collectors.toList());

    TreeMap<Integer, CustomerRepresentativeDto> requestCountCrMap = new TreeMap<>();
    String query = getQuery(filteredIds);
    ElasticSearchResponseDto elasticSearchResponseDto = elasticSearchService.entitySearch(
        "freeflowentities", query);
    log.debug("Customer Representative Request count,{}", elasticSearchResponseDto);
    List<CustomerRepresentativeDto> representativeDtos = new ArrayList<>();
    if (Objects.nonNull(elasticSearchResponseDto) && Objects.nonNull(
        elasticSearchResponseDto.getAggregations()) && elasticSearchResponseDto
        .getAggregations().getAggValue().getNestedAgg().getBuckets().size() > 0) {
      List<Bucket> buckets = elasticSearchResponseDto.getAggregations().getAggValue().getNestedAgg()
          .getBuckets();
      log.debug("Aggregated result", buckets);
      Map<String, Integer> userIdCountMap = buckets.stream()
          .collect(Collectors.toMap(Bucket::getKey, Bucket::getDoc_count));

      for (CustomerRepresentativeDto customerRepresentativeDto : customerRepresentativeDtos) {
        Optional<AdditionalDetail> additionalDetailOptional = customerRepresentativeDto.getAdditionalDetails()
            .stream().
            filter(each -> each.getRefType().equals("nfc_user_id")).findFirst();
        if (additionalDetailOptional.isPresent() && Objects.nonNull(
            additionalDetailOptional.get().getRefValue()) && !additionalDetailOptional.get()
            .getRefValue().isEmpty()) {
          if (userIdCountMap.containsKey(
              additionalDetailOptional.get().getRefValue())) {
            requestCountCrMap.put(userIdCountMap.get(
                additionalDetailOptional.get().getRefValue()), customerRepresentativeDto);
          } else {
            requestCountCrMap.put(0, customerRepresentativeDto);
          }
        }
      }
      if (requestCountCrMap.size() > 0) {
        representativeDtos.add(requestCountCrMap.firstEntry().getValue());
        return representativeDtos;
      }

    }
    representativeDtos.add(customerRepresentativeDtos.get(0));

    return representativeDtos;
  }

  public boolean shouldRun(List<String> configEnums) {
    return configEnums.contains(ROUND_ROBIN_CONFIG_NAME);
  }

  private String getQuery(List<String> filteredIds) {
    String commaSepratedIds = filteredIds.stream()
        .collect(Collectors.joining("\",\"", "\"", "\""));
    return "{\n"
        + "    \"query\": {\n"
        + "        \"bool\": {\n"
        + "            \"must\": [\n"
        + "                {\n"
        + "                    \"term\": {\n"
        + "                        \"payload.active\": true\n"
        + "                    }\n"
        + "                },\n"
        + "                {\n"
        + "                    \"nested\": {\n"
        + "                        \"path\": \"payload.freeFlowEntityPartyDTOList\",\n"
        + "                        \"query\": {\n"
        + "                            \"bool\": {\n"
        + "                                \"must\": [\n"
        + "                                    {\n"
        + "                                        \"terms\": {\n"
        + "                                            \"payload.freeFlowEntityPartyDTOList.userId\": [\n"
        + commaSepratedIds
        + "                                            ]\n"
        + "                                        }\n"
        + "                                    },\n"
        + "                                    {\n"
        + "                                        \"term\": {\n"
        + "                                            \"payload.freeFlowEntityPartyDTOList.partyType\": 3\n"
        + "                                        }\n"
        + "                                    }\n"
        + "                                ]\n"
        + "                            }\n"
        + "                        }\n"
        + "                    }\n"
        + "                }\n"
        + "            ]\n"
        + "        }\n"
        + "    },\n"
        + "    \"aggs\": {\n"
        + "        \"aggValue\": {\n"
        + "            \"nested\": {\n"
        + "                \"path\": \"payload.freeFlowEntityPartyDTOList\"\n"
        + "            },\n"
        + "            \"aggs\": {\n"
        + "                \"nestedAgg\": {\n"
        + "                    \"terms\": {\n"
        + "                        \"field\": \"payload.freeFlowEntityPartyDTOList.userId\"\n"
        + "                    }\n"
        + "                }\n"
        + "            }\n"
        + "        }\n"
        + "    }\n"
        + "}";
  }
}
