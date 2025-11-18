package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.converters.OrderManagementToRequestDtoConverter;
import com.ninjacart.nfcservice.dtos.ApprovalsDto;
import com.ninjacart.nfcservice.dtos.CommandCreationResponseDto;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandMessageBodyDto;
import com.ninjacart.nfcservice.dtos.FetchRequestResponseDto;
import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import com.ninjacart.nfcservice.dtos.MessageDto;
import com.ninjacart.nfcservice.dtos.OrderManagementRequestObjectDto;
import com.ninjacart.nfcservice.dtos.OrderRequestMessageDto;
import com.ninjacart.nfcservice.dtos.RequestCreationResponse;
import com.ninjacart.nfcservice.dtos.request.Message;
import com.ninjacart.nfcservice.dtos.request.RequestObjectDto;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
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

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ModifyOrderCommandService extends AbstractCommandCreationService{

    @Autowired
    private MatrixRestService matrixRestService;

    @Autowired private WorkflowRestService workflowRestService;

    @Autowired private OrderManagementToRequestDtoConverter orderManagementToRequestDtoConverter;

    private static final String DEFAULT_STATUS = OrderRequestCommandStatusEnum.PENDING.name();
    private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;
    private static final int VERSION_INCREMENT_VALUE = 1;
    private static final boolean TRUE = true;
    private static final String FUNCTION_RESPONSE = "Updated Successfully";
    private static final String ORDER_CREATION_ENTITY_STATUS = "ORDER_MODIFY_REQUEST";
    private static final String BUYER_TENANT_URL =
            "https://qa.ninjacart.in/workflow-engine/bcf02a74-bdd3-407f-9281-f50d070db431/5717/v1/execution/service/run/tenant-nfc-receive-order-request-creation";
    private static final String SELLER_TENANT_URL =
            "https://qa.ninjacart.in/workflow-engine/bcf02a74-bdd3-407f-9281-f50d070db431/5717/v1/execution/service/run/tenant-nfc-receive-order-request-creation";

    @Override
    public String getCommandType() {
        return CommandTypeEnum.MODIFY_ORDER.name();
    }

    @Override
    public CommandCreationResponseDto create(CommandCreationWrapperDto commandCreationWrapperDto)
            throws Exception {
        RequestObjectDto commandCreationObjectDto =
                parseAndCheckMandatoryValues(commandCreationWrapperDto);

        OrderManagementRequestObjectDto orderManagementRequestObjectDto =
                checkWhetherRequestIsAllowedOrNot(commandCreationObjectDto);

        deactivateOldFFE(orderManagementRequestObjectDto, commandCreationWrapperDto.getMessage().getInitiatedByDto());

        RequestCreationResponse requestCreationResponse =
                createOrderFFE(
                        orderManagementRequestObjectDto,
                        commandCreationObjectDto,
                        commandCreationWrapperDto.getMessage().getInitiatedByDto());

        triggerOrderRequestApprovalCommand(commandCreationObjectDto, requestCreationResponse, commandCreationWrapperDto.getMessage().getInitiatedByDto());

        return CommandCreationResponseDto.builder().status(FUNCTION_RESPONSE).build();
    }

    private RequestCreationResponse createOrderFFE(
            OrderManagementRequestObjectDto orderManagementRequestObjectDto,
            RequestObjectDto commandCreationObjectDto,
            InitiatedByDto initiatedByDto) {
        commandCreationObjectDto.setOwnerId(CommonConstants.DEFAULT_USERID);
        commandCreationObjectDto.setUserRoleDtos(orderManagementRequestObjectDto.getUserRoleDtoList());
        commandCreationObjectDto.setActive(true);

        List<ApprovalsDto> approvalsDtos = constructApprovals(commandCreationObjectDto, initiatedByDto);
        commandCreationObjectDto.setApprovals(approvalsDtos);
        commandCreationObjectDto.setEntityVersion(
                orderManagementRequestObjectDto.getEntityVersion() + VERSION_INCREMENT_VALUE);
        commandCreationObjectDto.setEntityStatus(ORDER_CREATION_ENTITY_STATUS);
        commandCreationObjectDto.setId(null);
        commandCreationObjectDto.setFreeFlowPartyDtos(orderManagementRequestObjectDto.getFreeFlowPartyDtos());
//        commandCreationObjectDto.setCreatedBy(initiatedByDto.getNfcUserId());
        return workflowRestService.createRequest(
                Message.builder().request(commandCreationObjectDto).build(), initiatedByDto.getNfcUserId());
    }

    private void deactivateOldFFE(OrderManagementRequestObjectDto orderManagementRequestObjectDto, InitiatedByDto initiatedByDto) {
        orderManagementRequestObjectDto.setActive(false);
        RequestObjectDto requestObjectDto =
                orderManagementToRequestDtoConverter.convertOmsResponseToFFERequest(
                        orderManagementRequestObjectDto);
        workflowRestService.createRequest(Message.builder().request(requestObjectDto).build(), initiatedByDto.getNfcUserId());
    }

    private OrderManagementRequestObjectDto checkWhetherRequestIsAllowedOrNot(
            RequestObjectDto commandCreationObjectDto) {
        FetchRequestResponseDto fetchRequestResponseDto =
                getOrderByExternalReferenceIdAndVersion(
                        commandCreationObjectDto.getExternalReferenceId(),
                        commandCreationObjectDto.getEntityVersion(),
                        TRUE);

        OrderManagementRequestObjectDto orderManagementRequestObjectDto =
                fetchRequestResponseDto.getRequests().get(0);

        if (orderManagementRequestObjectDto == null
                || orderManagementRequestObjectDto.getId() == null) {
            throw CommonUtils.logAndGetException("Something went wrong");
        }

        List<ApprovalsDto> approvalsDtos = orderManagementRequestObjectDto.getApprovals();

        if (!CollectionUtils.isEmpty(approvalsDtos)) {
            for (ApprovalsDto each : approvalsDtos) {
                if (StringUtils.isEmpty(each.getStatus())
                        || each.getStatus().equalsIgnoreCase(OrderRequestCommandStatusEnum.PENDING.name())) {
                    throw CommonUtils.logAndGetException("Order has pending approvals");
                }
            }
        }

        return orderManagementRequestObjectDto;
    }

    private RequestObjectDto parseAndCheckMandatoryValues(
            CommandCreationWrapperDto commandCreationWrapperDto) {
        if (commandCreationWrapperDto == null
                || commandCreationWrapperDto.getContext() == null
                || commandCreationWrapperDto.getMessage() == null
                || commandCreationWrapperDto.getMessage().getRequest() == null
                || commandCreationWrapperDto.getMessage().getInitiatedByDto() == null) {
            throw CommonUtils.logAndGetException("Invalid Input");
        }

        CommonUtils.checkInitiatedByMandatoryValues(
                commandCreationWrapperDto.getMessage().getInitiatedByDto());

        RequestObjectDto commandCreationObjectDto = parseAndGetBody(commandCreationWrapperDto);

        if (commandCreationObjectDto == null) {
            throw CommonUtils.logAndGetException("Invalid Input");
        }

        CommonUtils.checkUserMandatoryValues(commandCreationObjectDto.getSeller());
        CommonUtils.checkUserMandatoryValues(commandCreationObjectDto.getBuyer());
        CommonUtils.checkItemsMandatoryValues(commandCreationObjectDto.getItems());

        if (commandCreationObjectDto.getEntityVersion() == null
                || StringUtils.isEmpty(commandCreationObjectDto.getChatRoomId())
                || StringUtils.isEmpty(commandCreationObjectDto.getExternalReferenceId())) {
            throw CommonUtils.logAndGetException("Invalid Input");
        }

        if (StringUtils.isEmpty(commandCreationObjectDto.getDeliveryDate())) {
            throw CommonUtils.logAndGetException("Invalid DeliveryDate");
        }

        if (commandCreationObjectDto.getShippingAddress() == null) {
            throw CommonUtils.logAndGetException("Invalid Shipping Address");
        }


        if (StringUtils.isEmpty(commandCreationObjectDto.getChatRoomId())) {
            throw CommonUtils.logAndGetException("Invalid Input");
        }

        return commandCreationObjectDto;
    }

    private RequestObjectDto parseAndGetBody(CommandCreationWrapperDto commandCreationWrapperDto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(
                    commandCreationWrapperDto.getMessage().getRequest(), RequestObjectDto.class);
        } catch (Exception e) {
            throw CommonUtils.logAndGetException("Invalid Input", e);
        }
    }

    private void triggerApisToTenantBasedOnRole(CommandCreationWrapperDto commandCreationWrapperDto) {
        RoleEnum initiatorRole = commandCreationWrapperDto.getMessage().getInitiatedByDto().getRole();
        if (initiatorRole.equals(RoleEnum.ADMIN)) {
            commandCreationWrapperDto.getContext().setYourRole(RoleEnum.BUYER.name());
            forwardRequestToTenant(commandCreationWrapperDto, BUYER_TENANT_URL);

            commandCreationWrapperDto.getContext().setYourRole(RoleEnum.SELLER.name());
            forwardRequestToTenant(commandCreationWrapperDto, SELLER_TENANT_URL);
        } else if (initiatorRole.equals(RoleEnum.BUYER)
                || (initiatorRole.equals(RoleEnum.BUYER_ADMIN))) {
            commandCreationWrapperDto.getContext().setYourRole(RoleEnum.SELLER.name());
            forwardRequestToTenant(commandCreationWrapperDto, SELLER_TENANT_URL);
        } else if (initiatorRole.equals(RoleEnum.SELLER)
                || (initiatorRole.equals(RoleEnum.SELLER_ADMIN))) {
            commandCreationWrapperDto.getContext().setYourRole(RoleEnum.BUYER.name());
            forwardRequestToTenant(commandCreationWrapperDto, BUYER_TENANT_URL);
        } else if (initiatorRole.equals(RoleEnum.SELLER_BUYER_ADMIN)) {
            // TODO:
        }
    }

    private void triggerOrderRequestApprovalCommand(
            RequestObjectDto requestObjectDto,
            RequestCreationResponse requestCreationResponse,
            InitiatedByDto initiatedByDto) {
    OrderRequestMessageDto orderRequestMessageDto =
        OrderRequestMessageDto.builder()
            .userMessage(requestObjectDto.getMessage())
            .requestType(requestObjectDto.getRequestType())
            .items(requestObjectDto.getItems())
            .userRoles(requestCreationResponse.getUserRoleDtoList())
            .approvals(requestCreationResponse.getApprovals())
            .buyer(requestObjectDto.getBuyer())
            .seller(requestObjectDto.getSeller())
            .entityStatus(requestCreationResponse.getEntityStatus())
            .entityVersion(requestCreationResponse.getEntityVersion())
            .externalReferenceId(requestCreationResponse.getExternalReferenceId())
            .id(requestCreationResponse.getId())
            .requestType(requestCreationResponse.getRequestType())
            .deliveryDate(requestObjectDto.getDeliveryDate())
            .shippingAddress(requestObjectDto.getShippingAddress())
            .initiatedByDto(initiatedByDto)
            .qualityTerms(requestObjectDto.getQualityTerms())
            .paymentTerms(requestObjectDto.getPaymentTerms())
            .build();

        MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();

        CommandMessageBodyDto commandMessageBodyDto =
                CommandMessageBodyDto.builder()
                        .command(CommandTypeEnum.MODIFY_ORDER.name())
                        .data(orderRequestMessageDto)
                        .build();

        try {
            ObjectMapper mapper = new ObjectMapper();
            messageDto.setBody(mapper.writeValueAsString(commandMessageBodyDto));
        } catch (JsonProcessingException e) {
            throw CommonUtils.logAndGetException("Something went wrong", e);
        }
        // TODO: add ninjacart admin token
        matrixRestService.sendMessage(
                messageDto,
                requestObjectDto.getChatRoomId(),
                CommonUtils.generateMatrixUniqueTransactionId(RANDOM_NUMBER_GENERATOR_LENGTH),
                null);
    }

    private List<ApprovalsDto> constructApprovals(
            RequestObjectDto commandCreationObjectDto, InitiatedByDto initiatedByDto) {
        List<ApprovalsDto> approvalsDtos = new ArrayList<>();
        RoleEnum initiatorRole = initiatedByDto.getRole();
        if (initiatorRole.equals(RoleEnum.ADMIN)
                || initiatorRole.equals(RoleEnum.SELLER_ADMIN)
                || initiatorRole.equals(RoleEnum.BUYER_ADMIN)
                || initiatorRole.equals(RoleEnum.SELLER_BUYER_ADMIN)) {
            approvalsDtos.add(
                    constructAndReturnApprovalDto(
                            commandCreationObjectDto.getBuyer(), RoleEnum.BUYER.name(), OrderRequestCommandStatusEnum.PENDING.name()));
            approvalsDtos.add(
                    constructAndReturnApprovalDto(
                            commandCreationObjectDto.getSeller(), RoleEnum.SELLER.name(), OrderRequestCommandStatusEnum.PENDING.name()));
        } else if (initiatorRole.equals(RoleEnum.BUYER)) {
            approvalsDtos.add(
                    constructAndReturnApprovalDto(
                            commandCreationObjectDto.getSeller(), RoleEnum.SELLER.name(),  OrderRequestCommandStatusEnum.PENDING.name()));
            approvalsDtos.add(
                    constructAndReturnApprovalDto(
                            commandCreationObjectDto.getBuyer(), RoleEnum.BUYER.name(),  OrderRequestCommandStatusEnum.APPROVED.name()));
        } else if (initiatorRole.equals(RoleEnum.SELLER)) {
            approvalsDtos.add(
                    constructAndReturnApprovalDto(
                            commandCreationObjectDto.getBuyer(), RoleEnum.BUYER.name(),  OrderRequestCommandStatusEnum.PENDING.name()));
            approvalsDtos.add(
                    constructAndReturnApprovalDto(
                            commandCreationObjectDto.getSeller(), RoleEnum.SELLER.name(), OrderRequestCommandStatusEnum.APPROVED.name()));
        }

        return approvalsDtos;
    }

    private ApprovalsDto constructAndReturnApprovalDto(SellerDto dto, String role, String status) {
        return ApprovalsDto.builder()
                .role(role)
                .providerId(dto.getAppId())
                .status(status)
                .userId(dto.getUserId())
                .nfcUserId(dto.getNfcUserId())
                .build();
    }

    private void createOrderRequest(CommandCreationWrapperDto commandCreationWrapperDto) {
        String role = "";
        RoleEnum initiatorRole = commandCreationWrapperDto.getMessage().getInitiatedByDto().getRole();
        if (initiatorRole.equals(RoleEnum.ADMIN)) {
            role = RoleEnum.ADMIN.name();
        } else if (initiatorRole.equals(RoleEnum.BUYER)
                || (initiatorRole.equals(RoleEnum.BUYER_ADMIN))) {
            role = RoleEnum.BUYER.name();
        } else if (initiatorRole.equals(RoleEnum.SELLER)
                || (initiatorRole.equals(RoleEnum.SELLER_ADMIN))) {
            role = RoleEnum.SELLER.name();
        }
        commandCreationWrapperDto.getContext().setYourRole(role);
        workflowRestService.createOrderRequest(commandCreationWrapperDto);
    }
}
