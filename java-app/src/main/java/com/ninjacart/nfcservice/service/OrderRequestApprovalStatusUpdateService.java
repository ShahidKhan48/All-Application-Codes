package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.converters.OrderManagementToRequestDtoConverter;
import com.ninjacart.nfcservice.dtos.ApprovalsDto;
import com.ninjacart.nfcservice.dtos.CommandMessageBodyDto;
import com.ninjacart.nfcservice.dtos.CommandProcessorResponseDto;
import com.ninjacart.nfcservice.dtos.CommandRequestWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandStatusRequestObjectDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateWrapperDto;
import com.ninjacart.nfcservice.dtos.EditMessageDto;
import com.ninjacart.nfcservice.dtos.FetchRequestResponseDto;
import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import com.ninjacart.nfcservice.dtos.MatrixEventResponseDto;
import com.ninjacart.nfcservice.dtos.MessageDto;
import com.ninjacart.nfcservice.dtos.MessageResponse;
import com.ninjacart.nfcservice.dtos.OrderCreatedCommandDto;
import com.ninjacart.nfcservice.dtos.OrderManagementRequestObjectDto;
import com.ninjacart.nfcservice.dtos.OrderRequestCommandApprovalDto;
import com.ninjacart.nfcservice.dtos.OrderRequestMessageDto;
import com.ninjacart.nfcservice.dtos.RequestCreationResponse;
import com.ninjacart.nfcservice.dtos.request.Context;
import com.ninjacart.nfcservice.dtos.request.Message;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.dtos.request.RequestObjectDto;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.enums.MsgTypeEnum;
import com.ninjacart.nfcservice.enums.OrderRequestCommandStatusEnum;
import com.ninjacart.nfcservice.enums.RoleEnum;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class OrderRequestApprovalStatusUpdateService extends AbstractCommandProcessorService {

  @Autowired private WorkflowRestService workflowRestService;

  @Autowired private MatrixRestService matrixRestService;

  @Autowired private OrderManagementToRequestDtoConverter orderManagementToRequestDtoConverter;

  private static final String BUYER_TENANT_URL =
      "https://qa.ninjacart.in/workflow-engine/bcf02a74-bdd3-407f-9281-f50d070db431/5717/v1/execution/service/run/tenant-destination-order-request-approval";
  private static final String SELLER_TENANT_URL =
      "https://qa.ninjacart.in/workflow-engine/bcf02a74-bdd3-407f-9281-f50d070db431/5717/v1/execution/service/run/tenant-destination-order-request-approval";
  private static final String APPROVED_STATUS = "APPROVED";
  private static final boolean TRUE = true;
  private static final String ORDER_APPROVED_STATUS = "ORDER_REQUEST_APPROVED";
  private static final String ORDER_REJECTED_STATUS = "ORDER_REQUEST_REJECTED";

  private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;
  private static final String FUNCTION_RESPONSE = "Updated Successfully";
  private static final int VERSION_INCREMENT_VALUE = 1;
  private static final int APPROVED_STATUS_OMS = 2;

  private static final String BUYER_ORDER_CREATION_TENANT_URL =
          "https://qa.ninjacart.in/workflow-engine/bcf02a74-bdd3-407f-9281-f50d070db431/5717/v1/execution/service/run/tenant-order-creation";
  private static final String SELLER_ORDER_CREATION_TENANT_URL =
          "https://qa.ninjacart.in/workflow-engine/bcf02a74-bdd3-407f-9281-f50d070db431/5717/v1/execution/service/run/tenant-order-creation";

  @Override
  public String getCommandType() {
    return CommandTypeEnum.ORDER_REQUEST_APPROVAL.name();
  }

  @Override
  public CommandProcessorResponseDto processV1(
      CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) throws Exception {
    CommandStatusUpdateDto commandStatusUpdateDto = parseAndCheckMandatoryValues(commandStatusUpdateWrapperDto);

    CommandStatusRequestObjectDto statusUpdateDtoRequest = commandStatusUpdateDto.getRequest();

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

    RequestCreationResponse requestCreationResponse = updateDataInOms(statusUpdateDtoRequest, commandStatusUpdateDto.getInitiatedByDto(), orderManagementRequestObjectDto);

    getAndUpdateTheMessageWithStatus(
            statusUpdateDtoRequest.getCommandEventId(),
            statusUpdateDtoRequest.getRoomId(),
            null,
            requestCreationResponse);

    sendAudit(statusUpdateDtoRequest.getRoomId(), commandStatusUpdateDto.getInitiatedByDto(), statusUpdateDtoRequest,requestCreationResponse);

    if(!StringUtils.isEmpty(requestCreationResponse.getEntityStatus()) && requestCreationResponse.getEntityStatus().equalsIgnoreCase(ORDER_APPROVED_STATUS)) {
      triggerOrderCreatedMessage(statusUpdateDtoRequest.getExternalReferenceId(), statusUpdateDtoRequest.getRoomId(), requestCreationResponse);
    }

    return CommandProcessorResponseDto.builder().status(FUNCTION_RESPONSE).build();
  }

  private MessageResponse getAndUpdateTheMessageWithStatus(
          String eventId,
          String roomId,
          String token,
          RequestCreationResponse requestCreationResponse)
          throws Exception {
    MatrixEventResponseDto matrixEventResponseDto =
            matrixRestService.getEventByIdAndRoomId(roomId, eventId, token);
    CommandMessageBodyDto commandMessageBodyDto =
            parseAndGetCommandBody(matrixEventResponseDto.getContent().getBody());
    OrderRequestMessageDto orderRequestMessageDto =
            convertObjectToDto(commandMessageBodyDto.getData());
    orderRequestMessageDto.setEntityVersion(requestCreationResponse.getEntityVersion());
    orderRequestMessageDto.setApprovals(requestCreationResponse.getApprovals());
    commandMessageBodyDto.setData(orderRequestMessageDto);
    EditMessageDto editMessageDto =
            constructMessageReplaceDto(matrixEventResponseDto, commandMessageBodyDto);
    return matrixRestService.editMessage(
            editMessageDto,
            roomId,
            CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH),
            token);
  }

  private OrderRequestMessageDto convertObjectToDto(Object data) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.convertValue(data, OrderRequestMessageDto.class);
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }
  }

  private RequestCreationResponse updateDataInOms(CommandStatusRequestObjectDto statusUpdateDtoRequest, InitiatedByDto initiatedByDto, OrderManagementRequestObjectDto orderManagementRequestObjectDto) {
    List<ApprovalsDto> approvalsDtos = orderManagementRequestObjectDto.getApprovals();
    boolean approved =
            statusUpdateDtoRequest
                    .getStatus()
                    .equals(OrderRequestCommandStatusEnum.APPROVED.name());

    approvalsDtos =
            updateApprovals(
                    approvalsDtos, approved, initiatedByDto.getNfcUserId());
    orderManagementRequestObjectDto.setApprovals(approvalsDtos);

    orderManagementRequestObjectDto = checkApprovalStatusAndUpdateData(orderManagementRequestObjectDto, statusUpdateDtoRequest);
    orderManagementRequestObjectDto.setEntityVersion(orderManagementRequestObjectDto.getEntityVersion()+VERSION_INCREMENT_VALUE);
    RequestObjectDto requestObjectDto =
            orderManagementToRequestDtoConverter.convertOmsResponseToFFERequest(
                    orderManagementRequestObjectDto);

    return workflowRestService.createRequest(Message.builder().request(requestObjectDto).build(), initiatedByDto.getNfcUserId());

  }

  private OrderManagementRequestObjectDto checkApprovalStatusAndUpdateData(OrderManagementRequestObjectDto orderManagementRequestObjectDto, CommandStatusRequestObjectDto statusUpdateDtoRequest) {
    boolean approved = true;

    if(!CollectionUtils.isEmpty(orderManagementRequestObjectDto.getApprovals())) {
      for(ApprovalsDto each : orderManagementRequestObjectDto.getApprovals()) {
        if(StringUtils.isEmpty(each.getStatus()) || !each.getStatus().equalsIgnoreCase(OrderRequestCommandStatusEnum.APPROVED.name())) {
          approved = false;
          break;
        }
      }
    }

    if(approved) {
      orderManagementRequestObjectDto.setEntityStatus(ORDER_APPROVED_STATUS);
      orderManagementRequestObjectDto.setStatus(APPROVED_STATUS_OMS);
    }
    if (statusUpdateDtoRequest
            .getStatus()
            .equalsIgnoreCase(OrderRequestCommandStatusEnum.REJECTED.name())) {
      orderManagementRequestObjectDto.setEntityStatus(ORDER_REJECTED_STATUS);
    }

    return orderManagementRequestObjectDto;
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

    if(requestObjectDto == null || requestObjectDto.getEntityVersion() == null ||  StringUtils.isEmpty(requestObjectDto.getCommandEventId()) ||  StringUtils.isEmpty(requestObjectDto.getRoomId()) ||  StringUtils.isEmpty(requestObjectDto.getExternalReferenceId())) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }

    if(StringUtils.isEmpty(commandStatusUpdateDto.getRequest().getStatus())) {
      throw CommonUtils.logAndGetException("Status is empty");
    }

    return commandStatusUpdateDto;

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
    // TODO : REMOVE THIS FN
    return new CommandProcessorResponseDto();
  }

  private void checkApprovalStatusAndCreateOrder(CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) throws Exception {

//    if(!commandStatusUpdateWrapperDto.getMessage().getRequest().getStatus().equals(APPROVED_STATUS)) {
//      return;
//    }
//
//    List<ConsentsFetchResponseDto> consentsFetchResponseDtos = workflowRestService.fetchConsents(OrderManagementFetchFilter.builder()
//            .externalReference(commandStatusUpdateWrapperDto.getMessage().getRequest().getExternalReferenceId())
//            .build());
//
//    if(CollectionUtils.isEmpty(consentsFetchResponseDtos)) {
//      return;
//    }
//
//    for(ConsentsFetchResponseDto each: consentsFetchResponseDtos) {
//      if(each.getConsentStatus().equals(APPROVED_STATUS)) {
//        ObjectMapper mapper = new ObjectMapper();
//        triggerTenantApis(mapper.readValue(each.getConsentData(), RequestObjectDto.class), Context.builder()
//                .action("order_request_approval")
//                .provideId("1")
//                .transactionId("123")
//                .yourRole("BUYER")
//                .build());
//        triggerOrderCreatedMessage(commandStatusUpdateWrapperDto.getMessage().getRequest().getExternalReferenceId(), commandStatusUpdateWrapperDto.getMessage().getRequest().getRoomId());
//        break;
//      }
//    }
  }

  private void triggerOrderCreatedMessage(String externalReferenceId, String roomId, RequestCreationResponse requestCreationResponse) {

    CommandMessageBodyDto commandMessageBodyDto =
        CommandMessageBodyDto.builder()
            .command(CommandTypeEnum.ORDER_CREATED.name())
            .data(OrderRequestMessageDto.builder()
                    .orderId(externalReferenceId)
                    .entityVersion(requestCreationResponse.getEntityVersion())
                    .entityStatus(requestCreationResponse.getEntityStatus())
                    .id(requestCreationResponse.getId())
                    .build())
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

  private void triggerTenantApis(RequestObjectDto requestObjectDto, Context context) {
    context.setYourRole(RoleEnum.SELLER.name());
    forwardRequestToTenant(RequestDto.builder()
            .context(context)
            .message(Message.builder()
                    .request(requestObjectDto)
                    .build()).build(), SELLER_ORDER_CREATION_TENANT_URL);

    context.setYourRole(RoleEnum.BUYER.name());
    forwardRequestToTenant(RequestDto.builder()
            .context(context)
            .message(Message.builder()
                    .request(requestObjectDto)
                    .build()).build(), BUYER_ORDER_CREATION_TENANT_URL);
  }

  private void triggerApisToTenantBasedOnRole(
      CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) {
//    RoleEnum initiatorRole =
//        commandStatusUpdateWrapperDto.getMessage().getInitiatedByDto().getRole();
//    if (initiatorRole.equals(RoleEnum.BUYER)) {
//      commandStatusUpdateWrapperDto.getContext().setYourRole(RoleEnum.SELLER.name());
//      forwardRequestToTenant(commandStatusUpdateWrapperDto, SELLER_TENANT_URL);
//    } else if (initiatorRole.equals(RoleEnum.SELLER)) {
//      commandStatusUpdateWrapperDto.getContext().setYourRole(RoleEnum.BUYER.name());
//      forwardRequestToTenant(commandStatusUpdateWrapperDto, BUYER_TENANT_URL);
//    }
  }

  private void editApprovalMessage(CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto)
      throws Exception {
//    MatrixEventResponseDto matrixEventResponseDto =
//        getMessageByEvent(
//            commandStatusUpdateWrapperDto.getMessage().getRequest().getCommandEventId(),
//            commandStatusUpdateWrapperDto.getMessage().getRequest().getRoomId(),
//            null);
//    CommandMessageBodyDto commandMessageBodyDto =
//        parseAndGetCommandBody(matrixEventResponseDto.getContent().getBody());
//    ObjectMapper mapper = new ObjectMapper();
//    OrderRequestCommandApprovalDto orderRequestCommandApprovalDto =
//        mapper.convertValue(commandMessageBodyDto.getData(), OrderRequestCommandApprovalDto.class);
//
//    orderRequestCommandApprovalDto = updateApprovalStatus(orderRequestCommandApprovalDto, commandStatusUpdateWrapperDto);
//
//    commandMessageBodyDto.setData(orderRequestCommandApprovalDto);
//
//    EditMessageDto editMessageDto = constructMessageReplaceDto(matrixEventResponseDto, commandMessageBodyDto);
//
//    matrixRestService.editMessage(
//            editMessageDto,
//            commandStatusUpdateWrapperDto.getMessage().getRequest().getRoomId(),
//            CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH),
//            null);
  }

  private OrderRequestCommandApprovalDto updateApprovalStatus(
      OrderRequestCommandApprovalDto orderRequestCommandApprovalDto,
      CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) {
//    List<ApprovalsDto> approvalsDtos = orderRequestCommandApprovalDto.getApprovals();
//
//    if (CollectionUtils.isEmpty(approvalsDtos)) {
//      return orderRequestCommandApprovalDto;
//    }
//
//    for (ApprovalsDto each : orderRequestCommandApprovalDto.getApprovals()) {
//      if (!StringUtils.isEmpty(each.getRole())
//          && each.getRole()
//              .equals(
//                  commandStatusUpdateWrapperDto
//                      .getMessage()
//                      .getInitiatedByDto()
//                      .getRole()
//                      .name())) {
//        each.setStatus(commandStatusUpdateWrapperDto.getMessage().getRequest().getStatus());
//        each.setDisableAction(true);
//      }
//    }

    return orderRequestCommandApprovalDto;
  }

  private void triggerOrderRequestStatus(CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) {

  }

  private void sendAudit( String roomId, InitiatedByDto initiatedByDto,CommandStatusRequestObjectDto commandStatusRequestObjectDto, RequestCreationResponse requestCreationResponse)  {
    OrderRequestMessageDto orderRequestMessageDto =
        OrderRequestMessageDto.builder()
            .buyer(commandStatusRequestObjectDto.getBuyer())
            .seller(commandStatusRequestObjectDto.getSeller())
            .entityStatus(requestCreationResponse.getEntityStatus())
            .entityVersion(commandStatusRequestObjectDto.getEntityVersion())
            .externalReferenceId(commandStatusRequestObjectDto.getExternalReferenceId())
            .id(commandStatusRequestObjectDto.getId())
            .initiatedByDto(initiatedByDto)
            .approvals(requestCreationResponse.getApprovals())
            .build();

    MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();

    CommandMessageBodyDto commandMessageBodyDto =
        CommandMessageBodyDto.builder()
            .command(CommandTypeEnum.ORDER_REQUEST_APPROVAL_AUDIT.name())
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
