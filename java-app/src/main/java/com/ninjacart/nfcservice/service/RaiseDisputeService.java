package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.dtos.*;
import com.ninjacart.nfcservice.entity.ProviderConfiguration;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.enums.MsgTypeEnum;
import com.ninjacart.nfcservice.exception.NFCServiceException;
import com.ninjacart.nfcservice.repository.ProviderConfigurationRepository;
import com.ninjacart.nfcservice.utils.CommonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RaiseDisputeService extends AbstractCommandProcessorService {

  @Autowired
  private MatrixRestService matrixRestService;

  @Autowired
  private GeneralRestService generalRestService;

  @Autowired
  private ProviderConfigurationRepository providerConfigurationRepository;
  @Autowired
  private OrderManagementRestService orderManagementRestService;
  private static final String OUTPUT_TEMPLATE = "nfc-fetch-request-filter_2";
  private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;


  @Override
  public String getCommandType() {
    return CommandTypeEnum.RAISE_DISPUTE.name();
  }


  @Override
  public CommandProcessorResponseDto processV1(
      CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) throws Exception {
    log.info("Processing order update,data:{}", commandStatusUpdateWrapperDto);
    ObjectMapper mapper = new ObjectMapper();

    RaiseDisputeDto raiseDisputeDto = mapper.convertValue(
        commandStatusUpdateWrapperDto.getMessage(), RaiseDisputeDto.class);
    log.info("Processing order update,converted data:{}", raiseDisputeDto);

    if (Objects.isNull(raiseDisputeDto) || Objects.isNull(raiseDisputeDto.getChatRoomId())
        || Objects.isNull(raiseDisputeDto.getEntityStatus()) || Objects.isNull(
        raiseDisputeDto.getEntityVersion()) || Objects.isNull(
        raiseDisputeDto.getExternalReferenceId())) {
      throw new NFCServiceException("Mandatory inputs missing.");
    }
    FetchRequestResponseDto fetchRequestResponseDto = getOrderByExternalReferenceIdAndVersion(
        raiseDisputeDto.getExternalReferenceId(), raiseDisputeDto.getEntityVersion(),
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
        .nextEntityStatus(raiseDisputeDto.getEntityStatus())
        .fetchFilter(freeFlowEntityFetchFilterDto)
        .statusComment(raiseDisputeDto.getDisputeMessage())
        .status(fetchRequestResponseDto.getRequests().get(0).getStatus())
        .nfcUserId(raiseDisputeDto.getInitiatedBy().getNfcUserId()).build();
    FetchRequestResponseDto fetchRequestResponseUpdateDto = orderManagementRestService.updateOrderStatus(
        freeFlowFetchUpdateStatusDto, OUTPUT_TEMPLATE);
    log.info("Updated order data,{}", fetchRequestResponseUpdateDto);
    log.info("triggering command");
//    triggerOrderUpdateStatusCommand(fetchRequestResponseUpdateDto, raiseDisputeDto);
    callProviderDisputeUrl(freeFlowFetchUpdateStatusDto.getProviderId(),fetchRequestResponseDto,raiseDisputeDto);
    return CommandProcessorResponseDto.builder().status("Order updated successfully").build();
  }


  private void triggerOrderUpdateStatusCommand(FetchRequestResponseDto fetchRequestResponseDto,
      RaiseDisputeDto raiseDisputeDto) {

    MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();
    CommandResponseDto commandResponseDto = CommandResponseDto.builder()
        .initiatedBy(raiseDisputeDto.getInitiatedBy())
        .buyer(fetchRequestResponseDto.getRequests().get(0).getBuyer())
        .seller(fetchRequestResponseDto.getRequests().get(0).getSeller())
        .entityStatus(fetchRequestResponseDto.getRequests().get(0).getEntityStatus())
        .entityVersion(fetchRequestResponseDto.getRequests().get(0).getEntityVersion())
        .statusComment(raiseDisputeDto.getDisputeMessage())
        .chatRoomId(fetchRequestResponseDto.getRequests().get(0).getChatRoomId())
        .externalReferenceId(fetchRequestResponseDto.getRequests().get(0).getExternalReferenceId())
        .build();
/*    OrderUpdateStatusCommandDto orderUpdateStatusCommandDto = OrderUpdateStatusCommandDto.builder()

        .initiatedBy(raiseDisputeDto.getInitiatedBy()).build();

    if (!fetchRequestResponseDto.getRequests().isEmpty()) {
      orderUpdateStatusCommandDto.setRequest(fetchRequestResponseDto.getRequests().get(0));
    }*/
    // log.info("Trigger command: {}", orderUpdateStatusCommandDto);
    CommandMessageBodyDto commandMessageBodyDto = CommandMessageBodyDto.builder()
        .command(CommandTypeEnum.RAISE_DISPUTE.name()).data(commandResponseDto)
        .build();

    try {
      ObjectMapper mapper = new ObjectMapper();
      messageDto.setBody(mapper.writeValueAsString(commandMessageBodyDto));
    } catch (JsonProcessingException e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    // TODO: add ninjacart admin token
    log.info("room id,{}", raiseDisputeDto.getChatRoomId());
    matrixRestService.sendMessage(messageDto, raiseDisputeDto.getChatRoomId(),
        CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH), null);


  }
  public void callProviderDisputeUrl(String providerId, FetchRequestResponseDto fetchRequestResponseDto, RaiseDisputeDto raiseDisputeDto){
    MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();
    ProviderDisputeDto providerDisputeDto = ProviderDisputeDto.builder()
            .initiatedBy(raiseDisputeDto.getInitiatedBy())
            .buyer(fetchRequestResponseDto.getRequests().get(0).getBuyer())
            .seller(fetchRequestResponseDto.getRequests().get(0).getSeller())
            .entityStatus(fetchRequestResponseDto.getRequests().get(0).getEntityStatus())
            .entityVersion(fetchRequestResponseDto.getRequests().get(0).getEntityVersion())
            .statusComment(raiseDisputeDto.getDisputeMessage())
            .chatRoomId(fetchRequestResponseDto.getRequests().get(0).getChatRoomId())
            .externalReferenceId(fetchRequestResponseDto.getRequests().get(0).getExternalReferenceId())
            .build();
    ProviderConfiguration providerConfiguration = providerConfigurationRepository.findFirstByProviderIdAndActiveTrue(providerId);
    if(Objects.nonNull(providerConfiguration.getDisputeApiUrl())  && Objects.nonNull(providerConfiguration.getDisputeApiAuthToken())){
      generalRestService.providerDisputeUrl(providerConfiguration.getDisputeApiUrl(),providerConfiguration.getDisputeApiAuthToken(),providerDisputeDto);
    }
  }

  @Override
  public CommandProcessorResponseDto process(CommandRequestWrapperDto commandRequestWrapperDto)
      throws Exception {
    return null;
  }

}
