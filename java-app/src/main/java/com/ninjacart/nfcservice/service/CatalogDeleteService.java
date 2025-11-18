package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.CommandCreationResponseDto;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.dtos.CommunityCatalogSearchResponseDto;
import com.ninjacart.nfcservice.dtos.MessageResponse;
import com.ninjacart.nfcservice.dtos.catalog.*;
import com.ninjacart.nfcservice.dtos.elasticSearch.Bool;
import com.ninjacart.nfcservice.dtos.elasticSearch.Must;
import com.ninjacart.nfcservice.dtos.elasticSearch.Query;
import com.ninjacart.nfcservice.dtos.elasticSearch.SearchFilterDTO;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.helpers.RestClientHelper;
import com.ninjacart.nfcservice.helpers.configuration.ApplicationConfiguration;
import com.ninjacart.nfcservice.helpers.configuration.RestEndPoints;
import com.ninjacart.nfcservice.utils.CommonUtils;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CatalogDeleteService extends AbstractCommandDeletionService {

  @Autowired
  private RestEndPoints restEndPoints;

  private static final String FUNCTION_RESPONSE = "Deleted Successfully";

  private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;
  private static final String COMMUNITY_CATALOG = "CommunityCatalog";
  @Autowired
  private GemsRestService gemsRestService;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ApplicationConfiguration applicationConfiguration;


  @Override
  public CommandCreationResponseDto delete(CommandCreationWrapperDto commandCreationWrapperDto)
      throws Exception {
    if (commandCreationWrapperDto == null) {
      throw CommonUtils.logAndGetException("Bad Request");
    }
    ShareCatalogDeleteDto shareCatalogDeleteDto = parseAndGetBody(commandCreationWrapperDto);
    if (shareCatalogDeleteDto == null) {
      throw CommonUtils.logAndGetException("Bad Request");
    }
    deleteCatalogFromMatrix(shareCatalogDeleteDto.getChatRoomId(),
        shareCatalogDeleteDto.getEventId(),
        CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH));
    if (shareCatalogDeleteDto.getEventId() == null
        && shareCatalogDeleteDto.getChatRoomId() == null) {
      throw CommonUtils.logAndGetException("Bad Request");
    }
    List<CommunityCatalogSearchResponseDto> fetchResponse = fetchCatalogFromGems(
        shareCatalogDeleteDto.getEventId());
    if (CollectionUtils.isEmpty(fetchResponse)) {
      throw CommonUtils.logAndGetException("event id does not exist");
    }
    fetchResponse.get(0).getCommunityCatalogResponse().setStatus("CANCELLED");
    gemsRestService.updateGemsEntityById(fetchResponse.get(0).getCommunityCatalogResponse().getId(),
        CommonConstants.DEFAULT_REALM, String.valueOf(CommonConstants.DEFAULT_USERID),
        COMMUNITY_CATALOG, fetchResponse.get(0));

    return CommandCreationResponseDto.builder().status(FUNCTION_RESPONSE).build();
  }


  private List<CommunityCatalogSearchResponseDto> fetchCatalogFromGems(String eventId) {

    SearchFilterDTO searchFilterDTO = getSearchFilterDto(eventId);
    return fetchFromElastic(searchFilterDTO);
  }

  private List<CommunityCatalogSearchResponseDto> fetchFromElastic(
      SearchFilterDTO searchFilterDTO) {

    Object object = gemsRestService.entityAdvancedSearch(CommonConstants.DEFAULT_REALM,
        String.valueOf(CommonConstants.DEFAULT_USERID), COMMUNITY_CATALOG, searchFilterDTO);
    log.info("resp:{}", object.toString());
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      log.info("json:" + objectMapper.writeValueAsString(object));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    List<CommunityCatalogSearchResponseDto> catalogGemsResponseList = objectMapper.convertValue(
        object, new TypeReference<ArrayList<CommunityCatalogSearchResponseDto>>() {
        });
    log.info("list,{}", catalogGemsResponseList);

    return catalogGemsResponseList;
  }

  private SearchFilterDTO getSearchFilterDto(String eventId) {
    Must must = new Must();
    Map<String, Object> mustTermMap = new HashMap<>();
    mustTermMap.put("CommunityCatalog.eventId.keyword", eventId);
    must.setTerm(mustTermMap);
    return SearchFilterDTO.builder()
        .query(Query.builder().bool(Bool.builder().must(Arrays.asList(must)).build()).build())
        .size(100).build();
  }

  @Override
  public String getCommandType() {
    return CommandTypeEnum.CATALOG_DELETE.name();
  }

  private MessageResponse deleteCatalogFromMatrix(String chatRoomId, String eventId,
      String transactionId) {
    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withAuthorization(applicationConfiguration.getMatrixAdminToken())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>("{}", httpHeaders);
    ResponseEntity<MessageResponse> response = restTemplate.exchange(
        restEndPoints.getMatrixBaseUrl() + chatRoomId + "/redact/" + eventId + "/" + transactionId,
        HttpMethod.PUT, entity, MessageResponse.class);
    log.info(String.valueOf(response));
    return response.getBody();
  }


  private ShareCatalogDeleteDto parseAndGetBody(
      CommandCreationWrapperDto commandCreationWrapperDto) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.convertValue(commandCreationWrapperDto.getMessage().getRequest(),
          ShareCatalogDeleteDto.class);
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Invalid Input", e);
    }

  }


}
