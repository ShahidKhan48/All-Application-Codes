package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandMessageBodyDto;
import com.ninjacart.nfcservice.dtos.CommandProcessorResponseDto;
import com.ninjacart.nfcservice.dtos.CommandRequestWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateWrapperDto;
import com.ninjacart.nfcservice.dtos.FetchRequestResponseDto;
import com.ninjacart.nfcservice.dtos.FreeFlowEntityFetchFilterDto;
import com.ninjacart.nfcservice.dtos.FreeFlowFetchUpdateStatusDto;
import com.ninjacart.nfcservice.dtos.MessageDto;
import com.ninjacart.nfcservice.dtos.OrderRequestCommandApprovalDto;
import com.ninjacart.nfcservice.dtos.OrderStatusUpdateDto;
import com.ninjacart.nfcservice.dtos.OrderUpdateStatusCommandDto;
import com.ninjacart.nfcservice.dtos.UserRoleDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.enums.MsgTypeEnum;
import com.ninjacart.nfcservice.exception.NFCServiceException;
import com.ninjacart.nfcservice.utils.CommonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService extends AbstractCommandProcessorService {

  @Autowired
  private MatrixRestService matrixRestService;
  @Autowired
  private OrderManagementRestService orderManagementRestService;
  private static final String OUTPUT_TEMPLATE = "nfc-fetch-request-filter_2";
  private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;


  @Override
  public String getCommandType() {
    return CommandTypeEnum.ORDER_UPDATE_STATUS.name();
  }


  @Override
  public CommandProcessorResponseDto processV1(
      CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) {
    log.info("Processing order update,data:{}",commandStatusUpdateWrapperDto);
    ObjectMapper mapper = new ObjectMapper();

    OrderStatusUpdateDto orderStatusUpdateDto = mapper.convertValue(
        commandStatusUpdateWrapperDto.getMessage(), OrderStatusUpdateDto.class);
    log.info("Processing order update,converted data:{}",orderStatusUpdateDto);

    if (Objects.isNull(orderStatusUpdateDto) || Objects.isNull(orderStatusUpdateDto.getChatRoomId())
        || Objects.isNull(orderStatusUpdateDto.getEntityStatus()) || Objects.isNull(
        orderStatusUpdateDto.getEntityVersion()) || Objects.isNull(
        orderStatusUpdateDto.getExternalReferenceId()) ) {
      throw new NFCServiceException("Mandatory inputs missing.");
    }
    FetchRequestResponseDto fetchRequestResponseDto = getOrderByExternalReferenceIdAndVersion(
        orderStatusUpdateDto.getExternalReferenceId(), orderStatusUpdateDto.getEntityVersion(),
        true);
    log.info("Fetched order by ref and version,{}", fetchRequestResponseDto);
    if (Objects.isNull(fetchRequestResponseDto) || fetchRequestResponseDto.getRequests()
        .isEmpty()) {
      throw new NFCServiceException("Please refresh your page.No orders found");
    }
    List<Integer> ids = new ArrayList<>();
    ids.add(fetchRequestResponseDto.getRequests().get(0).getId());
    FreeFlowEntityFetchFilterDto freeFlowEntityFetchFilterDto = FreeFlowEntityFetchFilterDto.builder()
        .ids(ids).active(true).build();
    FreeFlowFetchUpdateStatusDto freeFlowFetchUpdateStatusDto = FreeFlowFetchUpdateStatusDto.builder()
        .providerId(commandStatusUpdateWrapperDto.getContext().getProviderId())
        .nextEntityStatus(orderStatusUpdateDto.getEntityStatus())
        .fetchFilter(freeFlowEntityFetchFilterDto)
        .status(fetchRequestResponseDto.getRequests().get(0).getStatus())

        .nfcUserId(orderStatusUpdateDto.getInitiatedBy().getNfcUserId())
    .build();
    FetchRequestResponseDto fetchRequestResponseUpdateDto = orderManagementRestService.updateOrderStatus(
        freeFlowFetchUpdateStatusDto, OUTPUT_TEMPLATE);
    log.info("Updated order data,{}", fetchRequestResponseUpdateDto);
    log.info("triggering command");
    triggerOrderUpdateStatusCommand(fetchRequestResponseUpdateDto, orderStatusUpdateDto);
    return CommandProcessorResponseDto.builder().status("Order updated successfully").build();
  }

  private void triggerOrderUpdateStatusCommand(FetchRequestResponseDto fetchRequestResponseDto,
      OrderStatusUpdateDto orderStatusUpdateDto) {

    MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();


    OrderUpdateStatusCommandDto orderUpdateStatusCommandDto = OrderUpdateStatusCommandDto.builder()

        .initiatedBy(orderStatusUpdateDto.getInitiatedBy()).build();

    if(!fetchRequestResponseDto.getRequests().isEmpty()) {
       orderUpdateStatusCommandDto.setRequest(fetchRequestResponseDto.getRequests().get(0));
    }
    log.info("Trigger command: {}", orderUpdateStatusCommandDto );
    CommandMessageBodyDto commandMessageBodyDto = CommandMessageBodyDto.builder()
        .command(CommandTypeEnum.ORDER_UPDATE_STATUS.name()).data(orderUpdateStatusCommandDto).build();

    try {
      ObjectMapper mapper = new ObjectMapper();
      messageDto.setBody(mapper.writeValueAsString(commandMessageBodyDto));
    } catch (JsonProcessingException e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    // TODO: add ninjacart admin token
    log.info("room id,{}", orderStatusUpdateDto.getChatRoomId());
    matrixRestService.sendMessage(messageDto, orderStatusUpdateDto.getChatRoomId(),
        CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH), null);


  }

  @Override
  public CommandProcessorResponseDto process(CommandRequestWrapperDto commandRequestWrapperDto)
      throws Exception {
    return null;
  }
}
