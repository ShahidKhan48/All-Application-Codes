package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.converters.OrderManagementToRequestDtoConverter;
import com.ninjacart.nfcservice.dtos.ApprovalsDto;
import com.ninjacart.nfcservice.dtos.CommandMessageBodyDto;
import com.ninjacart.nfcservice.dtos.CommandProcessorResponseDto;
import com.ninjacart.nfcservice.dtos.CommandRequestDto;
import com.ninjacart.nfcservice.dtos.CommandRequestWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandStatusRequestObjectDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateWrapperDto;
import com.ninjacart.nfcservice.dtos.ConsentStatusUpdateDto;
import com.ninjacart.nfcservice.dtos.EditMessageDto;
import com.ninjacart.nfcservice.dtos.FetchRequestResponseDto;
import com.ninjacart.nfcservice.dtos.FreeFlowEntityFetchFilterDto;
import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import com.ninjacart.nfcservice.dtos.MatrixEventResponseDto;
import com.ninjacart.nfcservice.dtos.MessageDto;
import com.ninjacart.nfcservice.dtos.MessageResponse;
import com.ninjacart.nfcservice.dtos.OrderManagementRequestObjectDto;
import com.ninjacart.nfcservice.dtos.OrderRequestMessageDto;
import com.ninjacart.nfcservice.dtos.RequestCreationResponse;
import com.ninjacart.nfcservice.dtos.WelcomeMessageDto;
import com.ninjacart.nfcservice.dtos.request.Context;
import com.ninjacart.nfcservice.dtos.request.Message;
import com.ninjacart.nfcservice.dtos.request.RequestObjectDto;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.enums.EntityStatusEnum;
import com.ninjacart.nfcservice.enums.MsgTypeEnum;
import com.ninjacart.nfcservice.enums.OmsStatusEnum;
import com.ninjacart.nfcservice.enums.OrderRequestCommandStatusEnum;
import com.ninjacart.nfcservice.helpers.configuration.ApplicationConfiguration;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class InitiateChatCommandProcessorService extends AbstractCommandProcessorService {

  @Autowired private ApplicationConfiguration applicationConfiguration;

  @Autowired private MatrixRestService matrixRestService;

  @Autowired private WorkflowRestService workflowRestService;

  @Autowired private OrderManagementToRequestDtoConverter orderManagementToRequestDtoConverter;

  private static final String TENANT_URL =
      "https://qa.ninjacart.in/workflow-engine/9c3db70d-5eb0-4265-995e-e2dcbb299722/5717/v1/execution/service/run/update-status-tenant";
  private Map<String, String> welcomeMessagePoints =
      new HashMap<String, String>() {
        {
          put("Rate", "Final price of the order");
          put("Quality", "Agreed quality of the product");
          put("Shipping", "Date and shipping arrangement");
          put("Payment", "Terms of the payment ");
        }
      };
  private static final String WELCOME_MESSAGE_FOOTER_NOTE =
      "Once all terms are discussed, click on \"+\" button to create the order.";
  private static final String WELCOME_MESSAGE_HEADING = "Important Notice";
  private static final String WELCOME_MESSAGE_SUB_HEADING =
      "Before closing the deal, please make sure you discuss below points. It is inportant concluding the deal.";
  private static final String FUNCTION_RESPONSE = "Updated Successfully";
  private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;
  private static final String CONSENT_TYPE = "NFC_NEGOTIATION_REQUEST";
  private static final String APPROVED = "APPROVED";
  private static final boolean TRUE = true;
  private static final int VERSION_INCREMENT_VALUE = 1;

  @Override
  public String getCommandType() {
    return CommandTypeEnum.INITIATE_CHAT_REQUEST.name();
  }

  @Override
  public CommandProcessorResponseDto processV1(
      CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) throws Exception {

    CommandStatusUpdateDto commandStatusUpdateDto = parseAndCheckMandatoryValues(commandStatusUpdateWrapperDto);

    CommandStatusRequestObjectDto statusUpdateDtoRequest = commandStatusUpdateDto.getRequest();

    boolean approved =
        statusUpdateDtoRequest
            .getEntityStatus()
            .equals(EntityStatusEnum.NEGOTIATION_APPROVED.name());

    FetchRequestResponseDto fetchRequestResponseDto =
        getOrderByExternalReferenceIdAndVersion(
            statusUpdateDtoRequest.getExternalReferenceId(),
            statusUpdateDtoRequest.getEntityVersion(),
            TRUE);

    OrderManagementRequestObjectDto orderManagementRequestObjectDto =
        fetchRequestResponseDto.getRequests().get(0);

    if (orderManagementRequestObjectDto == null
        || orderManagementRequestObjectDto.getId() == null) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }

    List<ApprovalsDto> approvalsDtos = orderManagementRequestObjectDto.getApprovals();

    approvalsDtos =
        updateApprovals(
            approvalsDtos, approved, commandStatusUpdateDto.getInitiatedByDto().getNfcUserId());

    boolean isAllApproved = checkWhetherAllApproved(approvalsDtos);

    orderManagementRequestObjectDto.setApprovals(approvalsDtos);
    orderManagementRequestObjectDto.setStatus(deriveOmsStatus(isAllApproved, approved, orderManagementRequestObjectDto.getStatus()));
    RequestCreationResponse requestCreationResponse =
        updateStatusInOms(statusUpdateDtoRequest, orderManagementRequestObjectDto, commandStatusUpdateDto.getInitiatedByDto(), isAllApproved);

    if (isAllApproved) {
      triggerWelcomeMessage(statusUpdateDtoRequest.getRoomId());
    }

    getAndUpdateTheMessageWithStatus(
        statusUpdateDtoRequest.getCommandEventId(),
        statusUpdateDtoRequest.getRoomId(),
        null,
        isAllApproved,
        approvalsDtos,
        requestCreationResponse);

    sendAudit(statusUpdateDtoRequest.getRoomId(), commandStatusUpdateDto.getInitiatedByDto(), statusUpdateDtoRequest, approvalsDtos);
    return CommandProcessorResponseDto.builder().status(FUNCTION_RESPONSE).build();
  }

  private Integer deriveOmsStatus(boolean isAllApproved, boolean approved, Integer existingStatus) {
    if(isAllApproved) {
      return OmsStatusEnum.APPROVED.getType();
    }
    else if(approved) {
      return existingStatus;
    }
    else {
      return OmsStatusEnum.REJECTED.getType();
    }
  }

  private String deriveOmsEntityStatus(boolean isAllApproved, String entityStatusFromUi, String existingStatus) {
    if(isAllApproved) {
      return EntityStatusEnum.NEGOTIATION_APPROVED.name();
    }
    else if(entityStatusFromUi.equals(EntityStatusEnum.NEGOTIATION_REJECTED.name())) {
      return EntityStatusEnum.NEGOTIATION_REJECTED.name();
    }
    else {
      return existingStatus;
    }
  }

  private boolean checkWhetherAllApproved(List<ApprovalsDto> approvalsDtos) {
    if(CollectionUtils.isEmpty(approvalsDtos)) {
      return true;
    }
    for(ApprovalsDto each : approvalsDtos) {
      if(StringUtils.isEmpty(each.getStatus()) || each.getStatus().equalsIgnoreCase(OrderRequestCommandStatusEnum.REJECTED.name()) || each.getStatus().equalsIgnoreCase(OrderRequestCommandStatusEnum.PENDING.name())) {
        return false;
      }
    }
    return true;
  }

  private CommandStatusUpdateDto parseAndCheckMandatoryValues(CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) {
    if (commandStatusUpdateWrapperDto == null
            || commandStatusUpdateWrapperDto.getMessage() == null
            || commandStatusUpdateWrapperDto.getContext() == null) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }

    CommandStatusUpdateDto commandStatusUpdateDto = parseAndGetBody(commandStatusUpdateWrapperDto);

    if(commandStatusUpdateDto == null) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }
    CommonUtils.checkInitiatedByMandatoryValues(commandStatusUpdateDto.getInitiatedByDto());

    CommandStatusRequestObjectDto requestObjectDto = commandStatusUpdateDto.getRequest();

    if(requestObjectDto == null || requestObjectDto.getEntityVersion() == null || StringUtils.isEmpty(requestObjectDto.getEntityStatus()) ||  StringUtils.isEmpty(requestObjectDto.getCommandEventId()) ||  StringUtils.isEmpty(requestObjectDto.getRoomId()) ||  StringUtils.isEmpty(requestObjectDto.getExternalReferenceId())) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }

    return commandStatusUpdateDto;

  }

  private MessageResponse getAndUpdateTheMessageWithStatus(
      String eventId,
      String roomId,
      String token,
      boolean approved,
      List<ApprovalsDto> approvalsDtos,
      RequestCreationResponse requestCreationResponse)
      throws Exception {
    MatrixEventResponseDto matrixEventResponseDto =
        matrixRestService.getEventByIdAndRoomId(roomId, eventId, token);
    CommandMessageBodyDto commandMessageBodyDto =
        parseAndGetCommandBody(matrixEventResponseDto.getContent().getBody());
    OrderRequestMessageDto orderRequestMessageDto =
        convertObjectToDto(commandMessageBodyDto.getData());
    orderRequestMessageDto.setEntityStatus(requestCreationResponse.getEntityStatus());
    orderRequestMessageDto.setEntityVersion(requestCreationResponse.getEntityVersion());
    orderRequestMessageDto.setApprovals(approvalsDtos);
//    commandMessageBodyDto.setDisableAction(true);
    commandMessageBodyDto.setBlockFurtherChat(!approved);
    commandMessageBodyDto.setData(orderRequestMessageDto);
    EditMessageDto editMessageDto =
        constructMessageReplaceDto(matrixEventResponseDto, commandMessageBodyDto);
    return matrixRestService.editMessage(
        editMessageDto,
        roomId,
        CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH),
        token);
  }

  private List<ApprovalsDto> updateApprovals(
      List<ApprovalsDto> approvalsDtos, boolean approved, Integer initiatedUserId) {
    if (CollectionUtils.isEmpty(approvalsDtos)) {
      return approvalsDtos;
    }

    for (ApprovalsDto each : approvalsDtos) {
      if (each.getNfcUserId() != null && Objects.equals(each.getNfcUserId(), initiatedUserId)) {
        if (approved) {
          each.setStatus(OrderRequestCommandStatusEnum.APPROVED.name());
        } else {
          each.setStatus(OrderRequestCommandStatusEnum.REJECTED.name());
        }
      }
    }

    return approvalsDtos;
  }

  private OrderRequestMessageDto convertObjectToDto(Object data) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.convertValue(data, OrderRequestMessageDto.class);
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }
  }

  private RequestCreationResponse updateStatusInOms(
      CommandStatusRequestObjectDto commandStatusRequestObjectDto,
      OrderManagementRequestObjectDto orderManagementRequestObjectDto,
      InitiatedByDto initiatedByDto,
      boolean isAllApproved) {
    RequestObjectDto requestObjectDto =
        orderManagementToRequestDtoConverter.convertOmsResponseToFFERequest(
            orderManagementRequestObjectDto);
    requestObjectDto.setEntityStatus(deriveOmsEntityStatus(isAllApproved, commandStatusRequestObjectDto.getEntityStatus(), orderManagementRequestObjectDto.getEntityStatus()));
    requestObjectDto.setEntityVersion(
        requestObjectDto.getEntityVersion() != null
            ? requestObjectDto.getEntityVersion() + VERSION_INCREMENT_VALUE
            : null);
    return workflowRestService.createRequest(
        Message.builder().request(requestObjectDto).build(), initiatedByDto.getNfcUserId());
  }

  private CommandStatusUpdateDto parseAndGetBody(
      CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.convertValue(
          commandStatusUpdateWrapperDto.getMessage(), CommandStatusUpdateDto.class);
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }
  }

  @Override
  public CommandProcessorResponseDto process(CommandRequestWrapperDto commandRequestWrapperDto)
      throws Exception {
    boolean approved = commandRequestWrapperDto.getRequest().getStatus().equals(APPROVED);
    //    ConsentStatusUpdateDto consentStatusUpdateDto =
    //        constructConsentStatusUpdateDto(commandRequestWrapperDto);
    //    // will update the status in order-management
    //    updateRequestStatus(consentStatusUpdateDto);
    //    // call tenant api
    //    CommandRequestWrapperDto tenantRequestBody =
    //        constructTenantWrapperDto(commandRequestWrapperDto);
    //    forwardRequestToTenant(tenantRequestBody, TENANT_URL);
    //    // trigger welcome message
    //    if (approved) {
    //      triggerWelcomeMessage(commandRequestWrapperDto);
    //    }
    // will fetch the message, updates the body and trigger the message update api
    // TODO : PASS ADMIN TOKEN
    //    getAndUpdateTheMessageWithActionTaken(
    //        commandRequestWrapperDto.getRequest().getCommandEventId(),
    //        commandRequestWrapperDto.getRequest().getRoomId(),
    //        null);

    return CommandProcessorResponseDto.builder().status(FUNCTION_RESPONSE).build();
  }

  private CommandRequestWrapperDto constructTenantWrapperDto(
      CommandRequestWrapperDto commandRequestWrapperDto) {
    return CommandRequestWrapperDto.builder()
        .context(commandRequestWrapperDto.getContext())
        .request(
            CommandRequestDto.builder()
                .referenceId(commandRequestWrapperDto.getRequest().getReferenceId())
                .status(commandRequestWrapperDto.getRequest().getStatus())
                .commandType(commandRequestWrapperDto.getRequest().getCommandType())
                .build())
        .build();
  }

  private ConsentStatusUpdateDto constructConsentStatusUpdateDto(
      CommandRequestWrapperDto commandRequestWrapperDto) {
    return ConsentStatusUpdateDto.builder()
        .consentTypeEnum(CONSENT_TYPE)
        .consentStatus(commandRequestWrapperDto.getRequest().getStatus())
        .refId(commandRequestWrapperDto.getRequest().getReferenceId())
        .build();
  }

  private void triggerWelcomeMessage(String roomId) {

    WelcomeMessageDto welcomeMessageDto = constructWelcomeMessage();
    CommandMessageBodyDto commandMessageBodyDto =
        CommandMessageBodyDto.builder()
            .command(CommandTypeEnum.WELCOME_MESSAGE.name())
            .data(welcomeMessageDto)
            .build();

    MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();

    try {
      ObjectMapper mapper = new ObjectMapper();
      messageDto.setBody(mapper.writeValueAsString(commandMessageBodyDto));
    } catch (JsonProcessingException e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    // TODO: add ninjacart admin token
    matrixRestService.sendMessage(
        messageDto,
        roomId,
        CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH),
        null);
  }

  private WelcomeMessageDto constructWelcomeMessage() {
    return WelcomeMessageDto.builder()
        .points(welcomeMessagePoints)
        .footerNote(WELCOME_MESSAGE_FOOTER_NOTE)
        .heading(WELCOME_MESSAGE_HEADING)
        .subHeading(WELCOME_MESSAGE_SUB_HEADING)
        .build();
  }

  private void sendAudit( String roomId, InitiatedByDto initiatedByDto,CommandStatusRequestObjectDto commandStatusRequestObjectDto, List<ApprovalsDto> approvalsDtos)  {
    OrderRequestMessageDto orderRequestMessageDto =
        OrderRequestMessageDto.builder()
            .buyer(commandStatusRequestObjectDto.getBuyer())
            .seller(commandStatusRequestObjectDto.getSeller())
            .entityStatus(commandStatusRequestObjectDto.getEntityStatus())
            .entityVersion(commandStatusRequestObjectDto.getEntityVersion())
            .externalReferenceId(commandStatusRequestObjectDto.getExternalReferenceId())
            .id(commandStatusRequestObjectDto.getId())
            .initiatedByDto(initiatedByDto)
            .approvals(approvalsDtos)
            .build();

    MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();

    CommandMessageBodyDto commandMessageBodyDto =
        CommandMessageBodyDto.builder()
            .command(CommandTypeEnum.INITIATE_CHAT_REQUEST_AUDIT.name())
            .data(orderRequestMessageDto)
            .build();

    try {
      ObjectMapper mapper = new ObjectMapper();
      messageDto.setBody(mapper.writeValueAsString(commandMessageBodyDto));
    } catch (JsonProcessingException e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    matrixRestService.sendMessage(
        messageDto,
        roomId,
        CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH),
        null);
    }
}

