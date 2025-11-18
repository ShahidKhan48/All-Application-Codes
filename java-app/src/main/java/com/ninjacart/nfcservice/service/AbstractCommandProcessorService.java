package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.dtos.CommandMessageBodyDto;
import com.ninjacart.nfcservice.dtos.CommandProcessorResponseDto;
import com.ninjacart.nfcservice.dtos.CommandRequestWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateWrapperDto;
import com.ninjacart.nfcservice.dtos.ConsentStatusUpdateDto;
import com.ninjacart.nfcservice.dtos.EditMessageDto;
import com.ninjacart.nfcservice.dtos.FetchRequestResponseDto;
import com.ninjacart.nfcservice.dtos.FreeFlowEntityFetchFilterDto;
import com.ninjacart.nfcservice.dtos.FreeFlowFetchUpdateStatusDto;
import com.ninjacart.nfcservice.dtos.MatrixEventResponseDto;
import com.ninjacart.nfcservice.dtos.MessageBodyDto;
import com.ninjacart.nfcservice.dtos.MessageRelateDto;
import com.ninjacart.nfcservice.dtos.MessageResponse;
import com.ninjacart.nfcservice.dtos.OrderManagementFetchFilter;
import com.ninjacart.nfcservice.enums.MsgTypeEnum;
import com.ninjacart.nfcservice.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public abstract class AbstractCommandProcessorService {

  @Autowired private OrderManagementRestService orderManagementRestService;

  @Autowired private TenantRestService tenantRestService;

  @Autowired private MatrixRestService matrixRestService;
  private static final String NFC_REQUEST_TYPE = "NFC_REQUEST";
  //TODO CHANGE
  private static final String OUTPUT_TEMPLATE = "nfc-fetch-request-filter_2";
  private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;

  public abstract String getCommandType();

  public abstract CommandProcessorResponseDto process(
      CommandRequestWrapperDto commandRequestWrapperDto) throws Exception;

  protected void updateRequestStatus(ConsentStatusUpdateDto consentStatusUpdateDto) {
    orderManagementRestService.updateConsentStatus(consentStatusUpdateDto);
  }

  protected void forwardRequestToTenant(Object requestBody, String url) {
    tenantRestService.triggerApiToTenant(requestBody, url);
  }

  protected MatrixEventResponseDto getMessageByEvent(String eventId, String roomId, String token) {
    return matrixRestService.getEventByIdAndRoomId(roomId, eventId, token);
  }

  protected CommandMessageBodyDto parseAndGetCommandBody(String messageBody) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(messageBody, CommandMessageBodyDto.class);
  }

  protected EditMessageDto constructMessageReplaceDto(
      MatrixEventResponseDto matrixEventResponseDto, CommandMessageBodyDto commandMessageBodyDto)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String body = mapper.writeValueAsString(commandMessageBodyDto);
    MessageRelateDto messageRelateDto =
        matrixEventResponseDto.getContent().getMessageRelateDto() != null
            ? matrixEventResponseDto.getContent().getMessageRelateDto()
            : MessageRelateDto.builder()
                .relationType(MsgTypeEnum.REPLACE.getType())
                .eventId(matrixEventResponseDto.getEventId())
                .build();
    MessageBodyDto messageBodyDto =
        MessageBodyDto.builder()
            .body(body)
            .msgtype(matrixEventResponseDto.getContent().getMsgtype())
            .build();
    return EditMessageDto.builder()
        .body(body)
        .msgtype(matrixEventResponseDto.getContent().getMsgtype())
        .messageBodyDto(messageBodyDto)
        .messageRelateDto(messageRelateDto)
        .build();
  }
  @LogExecutionTime
  public abstract CommandProcessorResponseDto processV1(
      CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) throws Exception;

  protected FetchRequestResponseDto getOrderByExternalReferenceIdAndVersion(
      String externalReferenceId,
      Integer entityVersion, Boolean active) {
    FetchRequestResponseDto fetchRequestResponseDto =
        fetchRequestByExternalReferenceAndVersion(externalReferenceId, entityVersion, active);

    if (fetchRequestResponseDto == null || CollectionUtils.isEmpty(
        fetchRequestResponseDto.getRequests())) {
      throw CommonUtils.logAndGetException(
          "Please refresh your page.No Entity Found for given external reference and version:"
              + externalReferenceId + ","
              + entityVersion);
    }
    return fetchRequestResponseDto;
  }

  protected FetchRequestResponseDto fetchRequestByExternalReferenceAndVersion(
      String externalReferenceId, Integer entityVersion, Boolean active) {
    return orderManagementRestService.fetchRequest(OrderManagementFetchFilter.builder()
        .externalReferenceId(externalReferenceId)
        .entityType(NFC_REQUEST_TYPE)
        .active(active)
        .entityVersion(entityVersion)
        .build(), OUTPUT_TEMPLATE);
  }




}
