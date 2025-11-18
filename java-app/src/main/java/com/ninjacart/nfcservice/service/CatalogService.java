package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.*;
import com.ninjacart.nfcservice.dtos.catalog.CommunityCatalog;
import com.ninjacart.nfcservice.dtos.catalog.CommunityCatalogGemsDTO;
import com.ninjacart.nfcservice.dtos.catalog.ShareCatalogCommandDto;
import com.ninjacart.nfcservice.dtos.catalog.ShareCatalogRequestDto;
import com.ninjacart.nfcservice.dtos.elasticSearch.*;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.enums.MsgTypeEnum;
import com.ninjacart.nfcservice.helpers.configuration.ApplicationConfiguration;
import com.ninjacart.nfcservice.utils.CommonUtils;
import java.text.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

//import static com.ninjacart.nfcservice.service.CommunityService.COMMUNITY_ENTITY;

@Service
@Slf4j
public class CatalogService extends AbstractCommandCreationService {

  private static final String COMMUNITY_ENTITY = "COMMUNITY_CATALOG";
  @Autowired
  private MatrixRestService matrixRestService;
  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private ApplicationConfiguration applicationConfiguration;

  @Autowired
  private GemsRestService gemsRestService;

  private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;

  private static final String FUNCTION_RESPONSE = "Shared Successfully";

  private static final String gemsURL = "https://nfc.ninjacart.in/entity-service/api/v1/realm/";
  private static final String COMMUNITY_CATALOG = "CommunityCatalog";

  @Override
  public String getCommandType() {
    return CommandTypeEnum.CATALOG_SHARE.name();
  }

  @Override
  public CommandCreationResponseDto create(CommandCreationWrapperDto commandCreationWrapperDto)
      throws Exception {
    ShareCatalogRequestDto catalogRequestDto = parseAndGetBody(commandCreationWrapperDto);

    Date expiryDate = DateUtils.addDays(new Date(), catalogRequestDto.getExpiryDays());

    catalogRequestDto.setExpiryAt(expiryDate);
    ShareCatalogCommandDto shareCatalogCommandDto = setShareCatalogCommandDto(catalogRequestDto,
        commandCreationWrapperDto);

    SearchFilterDTO searchQuery = getSearchFilterDto("CANCELLED", catalogRequestDto);
    Object communityCatalogListRaw = gemsRestService.entityAdvancedSearch(
        CommonConstants.DEFAULT_REALM, String.valueOf(CommonConstants.DEFAULT_USERID),
        COMMUNITY_CATALOG, searchQuery);
    ObjectMapper objectMapper = new ObjectMapper();

    log.info("query:{}",
        objectMapper.writeValueAsString(objectMapper.writeValueAsString(searchQuery)));
    try {
      log.info("json:" + objectMapper.writeValueAsString(communityCatalogListRaw));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    List<CommunityCatalogSearchResponseDto> catalogGemsResponseList = objectMapper.convertValue(
        communityCatalogListRaw, new TypeReference<ArrayList<CommunityCatalogSearchResponseDto>>() {
        });
    for (CommunityCatalogSearchResponseDto catalogSearchResponseDto : catalogGemsResponseList) {
      log.info("data,{}", catalogSearchResponseDto);
      CommunityCatalog communityCatalog = catalogSearchResponseDto.getCommunityCatalogResponse();
      communityCatalog.setExpired(true);
      GemsEntityResponseDto gemsEntityResponseDto = new GemsEntityResponseDto();
      gemsEntityResponseDto.setCommunityCatalog(communityCatalog);
      log.info("gems entity response,{}", gemsEntityResponseDto);
      GemsEntityResponseDto gemsEntityResponseDtoUpdate = gemsRestService.updateGemsEntityById(
          catalogSearchResponseDto.getCommunityCatalogResponse().getId(),
          CommonConstants.DEFAULT_REALM, String.valueOf(CommonConstants.DEFAULT_USERID),
          COMMUNITY_CATALOG, gemsEntityResponseDto);
      if (Objects.nonNull(gemsEntityResponseDto.getCommunityCatalog().getEventId())) {
        updateMatrix(gemsEntityResponseDto.getCommunityCatalog().getEventId(),
            catalogRequestDto.getChatRoomId());

      }
      log.info(String.valueOf(gemsEntityResponseDtoUpdate));
    }

    MessageResponse response = shareCatalogwithMatrix(shareCatalogCommandDto, catalogRequestDto);
    catalogRequestDto.getCommunityCatalog().setEventId(response.getEventId());
    catalogRequestDto.getCommunityCatalog().setRealmId(CommonConstants.DEFAULT_REALM);
    catalogRequestDto.getCommunityCatalog()
        .setUserId(Integer.toString(CommonConstants.DEFAULT_USERID));
    addCatalogToGems(catalogRequestDto);

    return CommandCreationResponseDto.builder().status(FUNCTION_RESPONSE).build();
  }

  private ShareCatalogCommandDto setShareCatalogCommandDto(ShareCatalogRequestDto catalogRequestDto,
      CommandCreationWrapperDto commandCreationWrapperDto) throws ParseException {
    ShareCatalogCommandDto shareCatalogCommandDto = new ShareCatalogCommandDto();
    shareCatalogCommandDto.setInitiatedByDto(
        commandCreationWrapperDto.getMessage().getInitiatedByDto());
    shareCatalogCommandDto.setCommunityCatalog(catalogRequestDto.getCommunityCatalog());
    shareCatalogCommandDto.getCommunityCatalog().setExpiryDays(catalogRequestDto.getExpiryDays());
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    System.out.println(dateFormat.format(catalogRequestDto.getExpiryAt()));
    log.info("date,{}", catalogRequestDto.getExpiryAt());
    shareCatalogCommandDto.getCommunityCatalog().setExpiryAt(catalogRequestDto.getExpiryAt());
    return shareCatalogCommandDto;
  }

  public void addCatalogToGems(ShareCatalogRequestDto catalogRequestDto) {
    CommunityCatalog communityCatalog = catalogRequestDto.getCommunityCatalog();
    CommunityCatalogGemsDTO communityCatalogGemsDTO = new CommunityCatalogGemsDTO();
    communityCatalog.setExpiryDays(catalogRequestDto.getExpiryDays());
    communityCatalog.setExpiryAt(catalogRequestDto.getExpiryAt());
    communityCatalog.setStatus("active");
    communityCatalogGemsDTO.setCommunityCatalog(communityCatalog);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CommunityCatalogGemsDTO> entity = new HttpEntity<>(communityCatalogGemsDTO, headers);
    ResponseEntity<Object> response = restTemplate.exchange(
        gemsURL + CommonConstants.DEFAULT_REALM + "/user/" + CommonConstants.DEFAULT_USERID
            + "/CommunityCatalog", HttpMethod.POST, entity, Object.class);
    log.debug(String.valueOf(response.getBody()));
  }

  MessageResponse shareCatalogwithMatrix(ShareCatalogCommandDto catalogRequestDto,
      ShareCatalogRequestDto requestDto) {
    MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();

    CommandMessageBodyDto commandMessageBodyDto = CommandMessageBodyDto.builder()
        .command(CommandTypeEnum.CATALOG_SHARE.name()).data(catalogRequestDto).build();

    try {
      ObjectMapper mapper = new ObjectMapper();
      messageDto.setBody(mapper.writeValueAsString(commandMessageBodyDto));
    } catch (JsonProcessingException e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return matrixRestService.sendMessage(messageDto, requestDto.getChatRoomId(),
        CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH), null);

  }

  private ShareCatalogRequestDto parseAndGetBody(
      CommandCreationWrapperDto commandCreationWrapperDto) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.convertValue(commandCreationWrapperDto.getMessage().getRequest(),
          ShareCatalogRequestDto.class);
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Invalid Input", e);
    }
  }

  private SearchFilterDTO getSearchFilterDto(String eventId,
      ShareCatalogRequestDto catalogRequestDto) {
    Must must1 = new Must();
    Must must2 = new Must();
    Must must3 = new Must();
    Must must4 = new Must();

    Map<String, Object> mustTermMap1 = new HashMap<>();
    Map<String, Object> mustTermMap2 = new HashMap<>();
    Map<String, Object> mustTermMap3 = new HashMap<>();
    Map<String, Object> mustTermMap4 = new HashMap<>();

    mustTermMap1.put("CommunityCatalog.status.keyword", "active");
    mustTermMap2.put("CommunityCatalog.expired", false);
    mustTermMap3.put("CommunityCatalog.community_id.keyword",
        catalogRequestDto.getCommunityCatalog().getCommunityId());
    mustTermMap4.put("CommunityCatalog.published_by.nfc_user_id.nfc_user_id",
        catalogRequestDto.getCommunityCatalog().getPublishedBy().getNfcUserId().getNfcUserId());
    must1.setTerm(mustTermMap1);
    must2.setTerm(mustTermMap2);
    must3.setTerm(mustTermMap3);
    must4.setTerm(mustTermMap4);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    String filterDate = StringUtils.join(StringUtils.split(dateFormat.format(new Date()), " "),
        "T");
    Date date = new Date();

    long unixTime = date.getTime() / 1000L;
    return SearchFilterDTO.builder().query(Query.builder().bool(
        Bool.builder().must(Arrays.asList(must1, must2, must3,must4))
            .filter(Filter.builder().range(new HashMap<String, Object>() {{
              put("CommunityCatalog.expiryAt", new HashMap<String, Object>() {{
                put("gte", unixTime);
              }});
            }}).build()).build()).build()).size(100).build();

  }

  public MessageResponse updateMatrix(String eventId, String roomId) throws Exception {
    MatrixEventResponseDto matrixEventResponseDto = matrixRestService.getEventByIdAndRoomId(roomId,
        eventId, applicationConfiguration.getMatrixAdminToken());

    CommandMessageBodyDto commandMessageBodyDto = parseAndGetCommandBody(
        matrixEventResponseDto.getContent().getBody());
    ShareCatalogCommandDto shareCatalogCommandDto = convertObjectToDto(
        commandMessageBodyDto.getData());
    shareCatalogCommandDto.getCommunityCatalog().setExpired(true);
    commandMessageBodyDto.setData(shareCatalogCommandDto);
    EditMessageDto editMessageDto = constructMessageReplaceDto(matrixEventResponseDto,
        commandMessageBodyDto);
    return matrixRestService.editMessage(editMessageDto, roomId,
        CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH), null);

  }

  protected EditMessageDto constructMessageReplaceDto(MatrixEventResponseDto matrixEventResponseDto,
      CommandMessageBodyDto commandMessageBodyDto) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String body = mapper.writeValueAsString(commandMessageBodyDto);
    MessageRelateDto messageRelateDto =
        matrixEventResponseDto.getContent().getMessageRelateDto() != null
            ? matrixEventResponseDto.getContent().getMessageRelateDto()
            : MessageRelateDto.builder().relationType(MsgTypeEnum.REPLACE.getType())
                .eventId(matrixEventResponseDto.getEventId()).build();
    MessageBodyDto messageBodyDto = MessageBodyDto.builder().body(body)
        .msgtype(matrixEventResponseDto.getContent().getMsgtype()).build();
    return EditMessageDto.builder().body(body)
        .msgtype(matrixEventResponseDto.getContent().getMsgtype()).messageBodyDto(messageBodyDto)
        .messageRelateDto(messageRelateDto).build();
  }

  private ShareCatalogCommandDto convertObjectToDto(Object data) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.convertValue(data, ShareCatalogCommandDto.class);
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }
  }

  protected CommandMessageBodyDto parseAndGetCommandBody(String messageBody) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(messageBody, CommandMessageBodyDto.class);
  }
}
