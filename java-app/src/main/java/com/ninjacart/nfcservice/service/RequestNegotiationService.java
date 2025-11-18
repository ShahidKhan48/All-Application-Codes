package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.*;
import com.ninjacart.nfcservice.dtos.request.*;
import com.ninjacart.nfcservice.entity.ProviderUser;

import com.ninjacart.nfcservice.dtos.CommandMessageBodyDto;
import com.ninjacart.nfcservice.dtos.MessageDto;
import com.ninjacart.nfcservice.dtos.OrderRequestMessageDto;
import com.ninjacart.nfcservice.dtos.UserRoleDto;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.enums.FreeFlowPartyTypeEnum;
import com.ninjacart.nfcservice.enums.MsgTypeEnum;
import com.ninjacart.nfcservice.enums.OrderRequestCommandStatusEnum;
import com.ninjacart.nfcservice.enums.ProviderConfigEnum;
import com.ninjacart.nfcservice.enums.RequestGroupStatusEnum;
import com.ninjacart.nfcservice.enums.RequestType;
import com.ninjacart.nfcservice.enums.RoleEnum;

import com.ninjacart.nfcservice.enums.RoomPresetEnum;
import com.ninjacart.nfcservice.dtos.requestGroup.RequestGroupDto;
import com.ninjacart.nfcservice.dtos.requestGroup.RequestGroupItems;
import com.ninjacart.nfcservice.enums.RoomVisibilityEnum;
import com.ninjacart.nfcservice.helpers.configuration.ApplicationConfiguration;

import com.ninjacart.nfcservice.service.crfilter.CrFilterService;
import com.ninjacart.nfcservice.notifications.FcmPushNotifier;
import com.ninjacart.nfcservice.notifications.PushNotificationRequest;
import com.ninjacart.nfcservice.repository.ProviderUserRepository;
import com.ninjacart.nfcservice.rest.IntegrationRestURL;

import com.ninjacart.nfcservice.utils.CommonUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestNegotiationService {

  private static final String CHAT_BASE_URL = "https://qa-nfc.ninjacart.in/ninja-agnet-web/home/chat?roomId=";
  @Autowired
  private IntegrationRestURL integrationRestURL;
  @Autowired
  private FcmPushNotifier fcmPushNotifier;
  @Autowired
  private UsersService usersService;
  @Autowired
  private RoomService roomService;


  @Autowired
  private OrderManagementRestService orderManagementRestService;

  @Autowired
  private WorkflowRestService workflowRestService;

  @Autowired
  private AccessTokenService accessTokenService;
  @Autowired
  private TenantRestService tenantRestService;

  @Autowired
  private MatrixRestService matrixRestService;

  @Autowired
  private ProvideruserService provideruserService;


  @Autowired
  private ProviderUserRepository providerUserRepository;

  @Autowired
  private ApplicationConfiguration applicationConfiguration;


  @Autowired
  private CrFilterService crFilterService;
  @Autowired
  private ProviderConfigurationService providerConfigurationService;
  private Map<String, String> providerToUrlMap =
      new HashMap<String, String>() {
        {
          put("1", "http://65.21.156.254:9899/tenant-nfc");
          put("2", "http://65.21.156.254:9899/tenant-nfc");
        }
      };

  private static final String USER_DEFAULT_PASSWORD = "NFC_9Hgre75352_6$#1@%7@_WERRW";
  private static final String DEFAULT_ROOM_TOPIC = "TRADE";

  public RequestGroupDto createRequestGroup(RequestDto requestDto) {
    log.debug("creating request group in nfc for requestDto : {}", requestDto);

    RequestGroupDto requestGroupDto = constructRequestGroupDto(requestDto);
    List<RequestGroupItems> items = constructAndAddItems(requestDto);
    requestGroupDto.setItems(items);

    List<RequestGroupDto> requestGroupDtos =
        orderManagementRestService.createRequestGroup(requestGroupDto);

    if (CollectionUtils.isEmpty(requestGroupDtos)) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }

    RequestGroupDto savedDto = requestGroupDtos.get(0);

    if (savedDto.getId() == null) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }

    return savedDto;
  }

  private List<RequestGroupItems> constructAndAddItems(RequestDto requestDto) {

    List<RequestGroupItems> items = new ArrayList<>();

    for (ItemsDto each : requestDto.getMessage().getRequest().getItems()) {
      RequestGroupItems requestGroupItems =
          RequestGroupItems.builder()
              .itemId(each.getId())
              .itemName(each.getName())
              .price(each.getPrice().getValue())
              .quantity(each.getQuantity().getCount())
              .imageUrl(each.getImageURL())
              .build();

      items.add(requestGroupItems);
    }

    return items;
  }

  private RequestGroupDto constructRequestGroupDto(RequestDto requestDto) {
    RequestGroupDto requestGroupDto =
        RequestGroupDto.builder()
            .requestType(requestDto.getMessage().getRequest().getRequestType().toString())
            .providerId(requestDto.getContext().getProviderId())
            .status(RequestGroupStatusEnum.RAISED.name())
            .message(requestDto.getMessage().getRequest().getMessage())
            .build();

    if (requestDto.getMessage().getRequest().getAdminInfo() != null) {
      requestGroupDto.setProviderUserId(
          requestDto.getMessage().getRequest().getAdminInfo().getUserId());
    } else if (requestGroupDto.getRequestType().equals(RequestType.BUY.getRequestType())) {
      requestGroupDto.setProviderUserId(
          requestDto.getMessage().getRequest().getBuyer().getUserId());
    } else if (requestGroupDto.getRequestType().equals(RequestType.SELL.getRequestType())) {
      requestGroupDto.setProviderUserId(
          requestDto.getMessage().getRequest().getSeller().getUserId());
    }

    return requestGroupDto;
  }

  public RequestCreationResponse createRequests(
      RequestDto requestDto, RequestGroupDto requestGroupDto, List<UserRoleDto> userRoleDtos,
      List<ApprovalsDto> approvals, List<FreeFlowPartyDto> freeFlowPartyDtos) {
    RequestObjectDto requestObjectDto = requestDto.getMessage().getRequest();
    requestObjectDto.setRequestGroupId(requestGroupDto.getId().toString());
    requestObjectDto.setOwnerId(CommonConstants.DEFAULT_USERID);
    requestObjectDto.setUserRoleDtos(userRoleDtos);
    requestObjectDto.setApprovals(approvals);
    requestObjectDto.setCopyIdToExternalReferenceId(true);
    requestObjectDto.setEntityVersion(0);
    requestObjectDto.setEntityStatus("NEGOTIATION_PENDING");
    requestObjectDto.setActive(true);
    requestObjectDto.setShippingAddress(Address.builder().build());
    Integer initiatedNfcUserId = requestDto.getMessage().getRequest().getBuyer().getNfcUserId();
    if (requestDto.getMessage().getRequest().getRequestType() == Integer.parseInt(
        RequestType.SELL.getRequestType())) {
      initiatedNfcUserId = requestDto.getMessage().getRequest().getSeller().getNfcUserId();
    }
    if (requestDto.getMessage().getRequest().isAdminAssistedFlow()) {
      initiatedNfcUserId = requestDto.getMessage().getRequest().getAdminInfo().getNfcUserId();
    }
    List<UserRoleDto> crDto = userRoleDtos.stream()
        .filter(each -> each.getRole().equals("CUSTOMERREPRESENTATIVE"))
        .collect(
            Collectors.toList());
    requestObjectDto.setCustomerRepresentativeDto(crDto);
    requestObjectDto.setFreeFlowPartyDtos(freeFlowPartyDtos);
    RequestCreationResponse requestCreationResponse = workflowRestService.createRequest(
        requestDto.getMessage(), initiatedNfcUserId);
    return requestCreationResponse;
  }

  public void triggerTenantApis(RequestDto requestDto, List<MatrixUserDto> matrixUserDtoList) {
    String baseUrl = "";

    MatrixUserDto sellerUserDto = null;
    MatrixUserDto buyerUserDto = null;
    for (MatrixUserDto each : matrixUserDtoList) {
      if (each.getRole().equals("BUYER")) {
        buyerUserDto = each;
      } else if (each.getRole().equals("SELLER")) {
        sellerUserDto = each;
      }
    }
    if (buyerUserDto == null || sellerUserDto == null) {
      throw new RuntimeException("Something went wrong");
    }
    if (requestDto.getMessage().getRequest().getRequestType()
        == Integer.parseInt(RequestType.BUY.getRequestType())) {
      requestDto = setMatrixValuesToRequest(requestDto, sellerUserDto);
      baseUrl = providerToUrlMap.get(requestDto.getMessage().getRequest().getSeller().getAppId());

    } else {
      requestDto = setMatrixValuesToRequest(requestDto, buyerUserDto);
      baseUrl = providerToUrlMap.get(requestDto.getMessage().getRequest().getBuyer().getAppId());
    }

    log.debug("baseUrl : {}", baseUrl);

    if (StringUtils.isEmpty(baseUrl)) {
      log.debug("no url for the provider so setting the default url");
      baseUrl = "http://65.21.156.254:9899/tenant-nfc";
    }

    tenantRestService.triggerTenantApi(requestDto, baseUrl);
  }

  private RequestDto setMatrixValuesToRequest(RequestDto requestDto, MatrixUserDto matrixUserDto) {
//    requestDto.getMessage().getRequest().setDeviceId(matrixUserDto.getMatrixDeviceId());
//    requestDto.getMessage().getRequest().setUserId(matrixUserDto.getMatrixUserId());
//    requestDto.getMessage().getRequest().setToken(matrixUserDto.getMatrixToken());
    return requestDto;
  }

  @LogExecutionTime
  public RequestDto requestNegotiation(RequestDto requestDto) {

    log.debug("requestDto : {}", requestDto);

    RequestGroupDto requestGroupDto = requestDto.getRequestGroupDto();
    if (requestGroupDto == null) {
      requestGroupDto = createRequestGroup(requestDto);
    }
    List<MatrixUserDto> matrixUserDtoList = getUserNames(requestDto);

    if (CollectionUtils.isEmpty(matrixUserDtoList)) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }

    List<Integer> nfcUserIds = matrixUserDtoList.stream().filter(e -> e.getNfcId() != null)
        .map(e -> e.getNfcId()).collect(Collectors.toList());

    Set<String> chatUserIds = getChatUserIds(nfcUserIds);

//    List<String> providerIdList = provideruserService.getProviderIds(matrixUserDtoList,
//        RoleEnum.BUYER.name());
//
//    log.debug("Process Started----------------------------------------------------------------");
//    CompletableFuture<List<CustomerRepresentativeDto>> cfBuyerCrs = CompletableFuture.supplyAsync(
//        () -> crFilterService.getAllCrs(
//
//            requestDto, providerIdList, true));
//    CompletableFuture<List<CustomerRepresentativeDto>> cfSellerCrs = CompletableFuture.supplyAsync(
//        () -> crFilterService.getAllCrs(requestDto,
//            provideruserService.getProviderIds(matrixUserDtoList, RoleEnum.SELLER.name()), false));
//    List<CustomerRepresentativeDto> customerRepresentativeDtos = new ArrayList<>();
//    List<CustomerRepresentativeDto> sellerCustomerRepresentativeDtos = new LinkedList<>();
//    try {
//      customerRepresentativeDtos = cfBuyerCrs.get();
//      log.info("buyer cr completes--------------------------------");
//      log.info("crs:{}", customerRepresentativeDtos);
//      sellerCustomerRepresentativeDtos = cfSellerCrs.get();
//      log.info("seller cr completes--------------------------------");
//      log.info("crs:{}", sellerCustomerRepresentativeDtos);
//
//    } catch (InterruptedException | ExecutionException e) {
//      log.error("Error while filtering CRs parallely,{} ", e.getMessage());
//      CommonUtils.logAndGetException("Error while filtering CRs parallely.");
//    }
////        customerRepresentativeDtos.addAll(crFilterService.getAllCrs(
////        requestDto, providerIdList, true));
////
////        customerRepresentativeDtos.addAll ( crFilterService.getAllCrs(
////        requestDto, provideruserService.getProviderIds(matrixUserDtoList, RoleEnum.SELLER.name()),
////        false));
//    customerRepresentativeDtos.addAll(sellerCustomerRepresentativeDtos);
    List<String> adminUserIds;
    List<MatrixUserDto> adminUserDtos = new ArrayList<MatrixUserDto>();
    ProviderIdDto providerIdDto = provideruserService.getProviderIdDto(matrixUserDtoList);

    Set<CustomerRepresentativeDto> uniqueCustomeRepresentativeDtos = crFilterService.getCustomerRepresentativeDtos(
        providerIdDto,
        requestDto);
    boolean isfallbackAdmin = false;
    if (!uniqueCustomeRepresentativeDtos.isEmpty()) {
      adminUserIds = uniqueCustomeRepresentativeDtos.stream()
          .map(e -> String.valueOf(e.getUserId())).collect(Collectors.toList());
      adminUserDtos = provideruserService.getAdminMatrixUserDtos(
          new ArrayList<String>(Collections.singleton(providerIdDto.getBuyerProviderId())),
          adminUserIds);
    } else {
      ProviderConfigurationDto providerConfigurationDto = providerConfigurationService.fetchProviderConfig(
          providerIdDto.getBuyerProviderId());
      if (providerConfigurationDto!=null && !providerConfigurationDto.getAssignmentTechnique()
          .equals(ProviderConfigEnum.MANUAL.name())) {
        adminUserDtos = provideruserService.getDefaultAdminMatrixUserDto(
            providerConfigurationDto.getFallbackAgentDetails());
        isfallbackAdmin = true;
      }

    }

    // List<MatrixUserDto> adminUserDtos = provideruserService.getAdminChatUserIds(matrixUserDtoList);

    List<String> adminChatUserIds = new ArrayList<>();

    if (!CollectionUtils.isEmpty(adminUserDtos)) {
      adminChatUserIds = adminUserDtos.stream()
          .filter(e -> !StringUtils.isEmpty(e.getMatrixUserId())).map(e -> e.getMatrixUserId())
          .collect(Collectors.toList());
      matrixUserDtoList.addAll(adminUserDtos);
    }

    if (!CollectionUtils.isEmpty(adminChatUserIds)) {
      chatUserIds.addAll(adminChatUserIds);
    }

    RoomCreationResponse roomCreationResponse =
        constructDtoAndCreateChatRoom(chatUserIds, requestDto);

    requestDto.getMessage().getRequest().setChatRoomId(roomCreationResponse.getRoomId());

    List<UserRoleDto> userRoleDtos = constructUserRoleDtos(matrixUserDtoList);
    List<ApprovalsDto> approvals =
        constructApprovalsForRequestNegotiation(
            matrixUserDtoList, requestDto.getMessage().getRequest());
    List<FreeFlowPartyDto> freeFlowPartyDtos = constructPartyDto(uniqueCustomeRepresentativeDtos,
        requestDto, adminUserDtos, isfallbackAdmin);
    RequestCreationResponse requestCreationResponse = createRequests(requestDto, requestGroupDto,
        userRoleDtos, approvals, freeFlowPartyDtos);

    triggerNegotiationRequestCommand(requestDto.getMessage().getRequest(), userRoleDtos, approvals,
        requestCreationResponse);

    return requestDto;
  }

  public List<FreeFlowPartyDto> constructPartyDto(
      Set<CustomerRepresentativeDto> customerRepresentativeDto,
      RequestDto requestDto, List<MatrixUserDto> adminUserDtos, boolean isFallbackAdmin) {
    List<FreeFlowPartyDto> freeFlowPartyDtos = new LinkedList<>();
    for (CustomerRepresentativeDto customerRepresentative : customerRepresentativeDto) {
      Optional<AdditionalDetail> optionalEmail = customerRepresentative.getAdditionalDetails()
          .stream().filter(each -> each.getRefType().equals("email")).findFirst();
      Optional<AdditionalDetail> optionalNfcUserId = customerRepresentative.getAdditionalDetails()
          .stream().filter(each -> each.getRefType().equals("nfc_user_id")).findFirst();
      Optional<AdditionalDetail> optionalPhone = customerRepresentative.getAdditionalDetails()
          .stream().filter(each -> each.getRefType().equals("contactNumber")).findFirst();

      FreeFlowPartyDto freeFlowPartyDto = FreeFlowPartyDto.builder()
          .name(customerRepresentative.getName())

          .partyType(FreeFlowPartyTypeEnum.CR.getId()).build();
      if (optionalEmail.isPresent()) {
        freeFlowPartyDto.setEmail(optionalEmail.get().getRefValue());
      }
      if (optionalNfcUserId.isPresent()) {
        try {
          freeFlowPartyDto.setNfcUserId(Integer.parseInt(optionalNfcUserId.get().getRefValue()));

        } catch (NumberFormatException nfe) {
          log.error("Error while setting nfc user id: " + optionalNfcUserId.get().getRefValue());
        }
      }
      if (optionalPhone.isPresent()) {
        freeFlowPartyDto.setPhone(optionalPhone.get().getRefValue());
      }
      freeFlowPartyDtos.add(freeFlowPartyDto);
    }
    SellerDto buyer = requestDto.getMessage().getRequest().getBuyer();
    FreeFlowPartyDto buyersPartyDto = FreeFlowPartyDto.builder().name(buyer.getName()).
        nfcUserId(buyer.getNfcUserId()).email(buyer.getEmail()).phone(buyer.getPhone())
        .partyType(FreeFlowPartyTypeEnum.BUYER.getId()).build();
    freeFlowPartyDtos.add(buyersPartyDto);

    SellerDto seller = requestDto.getMessage().getRequest().getSeller();
    FreeFlowPartyDto sellerPartyDto = FreeFlowPartyDto.builder().name(seller.getName()).
        nfcUserId(seller.getNfcUserId()).email(seller.getEmail()).phone(seller.getPhone())
        .partyType(FreeFlowPartyTypeEnum.SELLER.getId()).build();
    freeFlowPartyDtos.add(sellerPartyDto);

    FreeFlowPartyDto defaultAdminDto = FreeFlowPartyDto.builder()
        .nfcUserId(applicationConfiguration.getNfcAdminUserId())
        .partyType(FreeFlowPartyTypeEnum.ADMIN.getId()).build();
    freeFlowPartyDtos.add(defaultAdminDto);
    if (!adminUserDtos.isEmpty() && isFallbackAdmin) {
      FreeFlowPartyDto fallbackAdminDto = FreeFlowPartyDto.builder()
          .name(adminUserDtos.get(0).getUserName())
          .partyType(3).nfcUserId(adminUserDtos.get(0).getNfcId()).build();
      freeFlowPartyDtos.add(fallbackAdminDto);
    }
    return freeFlowPartyDtos;
  }

  private List<ApprovalsDto> constructApprovalsForRequestNegotiation(
      List<MatrixUserDto> matrixUserDtoList, RequestObjectDto requestObjectDto) {

    List<ApprovalsDto> approvals = new ArrayList<>();
    for (MatrixUserDto each : matrixUserDtoList) {
      if (each.getRole().equals(RoleEnum.BUYER.name()) || each.getRole()
          .equals(RoleEnum.SELLER.name())) {
        ApprovalsDto approvalsDto = ApprovalsDto.builder()
            .role(each.getRole())
            .providerId(each.getProviderId())
            .userId(each.getProviderUserId())
            .nfcUserId(each.getNfcId())
            .build();

        if (requestObjectDto.getRequestType() == Integer.parseInt(RequestType.BUY.getRequestType())
            && each.getRole().equals(RoleEnum.BUYER.name())
            && !requestObjectDto.isAdminAssistedFlow()) {
          approvalsDto.setStatus(OrderRequestCommandStatusEnum.APPROVED.name());
        } else if (
            requestObjectDto.getRequestType() == Integer.parseInt(RequestType.SELL.getRequestType())
                && each.getRole().equals(RoleEnum.SELLER.name())
                && !requestObjectDto.isAdminAssistedFlow()) {
          approvalsDto.setStatus(OrderRequestCommandStatusEnum.APPROVED.name());
        } else {
          approvalsDto.setStatus(OrderRequestCommandStatusEnum.PENDING.name());
        }
        approvals.add(approvalsDto);
      }
    }

    return approvals;
  }

  private Set<String> getChatUserIds(List<Integer> nfcUserIds) {
    List<ProviderUser> providerUsers = provideruserService.findByIds(nfcUserIds);
    if (CollectionUtils.isEmpty(nfcUserIds)) {
      throw CommonUtils.logAndGetException("Invalid UserIds");
    }
    return providerUsers.stream().filter(e -> !StringUtils.isEmpty(e.getChatUserId()))
        .map(e -> e.getChatUserId()).collect(Collectors.toSet());
  }

  private void triggerNegotiationRequestCommand(
      RequestObjectDto requestObjectDto, List<UserRoleDto> userRoleDtos,
      List<ApprovalsDto> approvals, RequestCreationResponse requestCreationResponse) {

    OrderRequestMessageDto orderRequestMessageDto =
        OrderRequestMessageDto.builder()
            .userMessage(requestObjectDto.getMessage())
            .requestType(requestObjectDto.getRequestType())
            .items(requestObjectDto.getItems())
            .userRoles(userRoleDtos)
            .approvals(approvals)
            .buyer(requestObjectDto.getBuyer())
            .seller(requestObjectDto.getSeller())
            .entityStatus(requestCreationResponse.getEntityStatus())
            .entityVersion(requestCreationResponse.getEntityVersion())
            .externalReferenceId(requestCreationResponse.getExternalReferenceId())
            .id(requestCreationResponse.getId())
            .adminAssistedFlow(requestObjectDto.isAdminAssistedFlow())
            .initiatedByDto(requestObjectDto.getAdminInfo())
            .build();

    CommandMessageBodyDto commandMessageBodyDto =
        CommandMessageBodyDto.builder()
            .command(CommandTypeEnum.INITIATE_CHAT_REQUEST.name())
            .data(orderRequestMessageDto)
            .blockFurtherChat(true)
            .build();

    MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();

    try {
      ObjectMapper mapper = new ObjectMapper();
      messageDto.setBody(mapper.writeValueAsString(commandMessageBodyDto));
    } catch (JsonProcessingException e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }

    String uniqueTransactionId = generateMatrixUniqueTransactionId();

    matrixRestService.sendMessageAsAdmin(
        messageDto, requestObjectDto.getChatRoomId(), uniqueTransactionId);
  }

  private List<UserRoleDto> constructUserRoleDtos(List<MatrixUserDto> matrixUserDtoList) {
    List<UserRoleDto> userRoleDtos = new ArrayList<>();

    for (MatrixUserDto each : matrixUserDtoList) {
      userRoleDtos.add(UserRoleDto.builder()
          .role(each.getRole())
          .userId(each.getProviderUserId())
          .nfcUserId(each.getNfcId())
          .providerId(each.getProviderId())
          .build());
    }

    userRoleDtos.add(UserRoleDto.builder()
        .userId(applicationConfiguration.getAdminUserId().toString())
        .providerId(applicationConfiguration.getNfcProviderId())
        .role(RoleEnum.ADMIN.name())
        .nfcUserId(applicationConfiguration.getNfcAdminUserId())
        .build());
    return userRoleDtos;
  }

  private String generateMatrixUniqueTransactionId() {
    return String.format(
        "%s.%s.%s", "m", new Date().getTime(), CommonUtils.randomNumberGenerator(10));
  }


  private RequestDto setReturnResponse(
      RequestDto requestDto, List<MatrixUserDto> matrixUserDtoList) {
    MatrixUserDto sellerUserDto = null;
    MatrixUserDto buyerUserDto = null;
    for (MatrixUserDto each : matrixUserDtoList) {
      if (each.getRole().equals(RoleEnum.BUYER.name())) {
        buyerUserDto = each;
      } else if (each.getRole().equals(RoleEnum.SELLER.name())) {
        sellerUserDto = each;
      }
    }
    if (requestDto.getMessage().getRequest().getRequestType()
        == Integer.parseInt(RequestType.SELL.getRequestType())) {
      requestDto = setMatrixValuesToRequest(requestDto, sellerUserDto);
    } else {
      requestDto = setMatrixValuesToRequest(requestDto, buyerUserDto);
    }
    log.debug("return response of the api : {}", requestDto);
    return requestDto;
  }

  private RoomCreationResponse constructDtoAndCreateChatRoom(
      Set<String> userNameToMatrixUserId, RequestDto requestDto) {
    RequestObjectDto requestObjectDto = requestDto.getMessage().getRequest();
    String name =
        String.format(
            "%s-%s-%s",
            requestObjectDto.getItems().get(0).getName(),
            requestObjectDto.getBuyer().getName(),
            requestObjectDto.getSeller().getName());
    return roomService.createAndJoinUsers(
        RoomCreationDto.builder()
            .creationContent(new FederationClass())
            .name(name)
            .preset(RoomPresetEnum.PRIVATE_CHAT.getType())
            .userIds(userNameToMatrixUserId)
            .topic(DEFAULT_ROOM_TOPIC)
            .visibility(RoomVisibilityEnum.PRIVATE.getType())
            .build());
  }

  private Map<String, OnboardDto> constructUserNameToOnboardDtoMap(
      List<MatrixUserDto> matrixUserDtoList) {
    Map<String, OnboardDto> matrixUserNameFormatToOnboardDtoMap = new HashMap<>();

    for (MatrixUserDto each : matrixUserDtoList) {
      OnboardDto onboardDto = constructOnboardDto(each);
      matrixUserNameFormatToOnboardDtoMap.put(
          each.getMatrixUserNameFormat(), onboardDto);
      each.setPassword(onboardDto.getPassword());
    }
    return matrixUserNameFormatToOnboardDtoMap;
  }

  private OnboardDto constructOnboardDto(MatrixUserDto matrixUserDto) {
    return OnboardDto.builder()
        .admin(matrixUserDto.isMatrixChatAdmin())
        .displayname(matrixUserDto.getUserName())
        .username(matrixUserDto.getMatrixUserNameFormat())
        .password(UUID.randomUUID().toString())
        .build();
  }

  private List<MatrixUserDto> getUserNames(RequestDto requestDto) {
    List<MatrixUserDto> matrixUserDtoList = new ArrayList<>();
    MatrixUserDto buyerMaxtrix = getBuyerMatrixUserDto(requestDto);
    MatrixUserDto sellerMatrix = getSellerMatrixUserDto(requestDto);
    matrixUserDtoList.add(buyerMaxtrix);
    matrixUserDtoList.add(sellerMatrix);
//    List<Admin> adminMatrix = provideruserService.getAdminsFromDb(buyerMaxtrix,sellerMatrix);
//    List<MatrixUserDto> adminUserDtos = getAdminUserDtos(adminMatrix,requestDto);
//
//    if (!CollectionUtils.isEmpty(adminUserDtos)) {
//      matrixUserDtoList.addAll(adminUserDtos);
//    }

    return matrixUserDtoList;
  }

  /* private List<MatrixUserDto> getAdminUserDtos(List<Admin> adminList,RequestDto requestDto) {
    List<MatrixUserDto> adminMatrixDtosList = new ArrayList<>();
    // TODO:use new table
    for(Admin admin : adminList) {
      String adminUserName = String.format(
              "%s_%s",
              admin.getProviderId(),
              admin.getUserId()
      );
      if(adminMatrixDtosList.contains(admin))
      {
        continue;
      }
      if(admin.getProviderId().equals("NfcAdmin")){
       adminMatrixDtosList.add( MatrixUserDto.builder()
                .matrixUserNameFormat(adminUserName)
                .userName(admin.getUserName())
                .providerId(admin.getProviderId())
                .providerUserId(admin.getUserId())
                .category(constructCategoryList(requestDto.getMessage().getRequest().getItems()))
                .nfcId(admin.getNfcId())
                .role(RoleEnum.ADMIN.name())
                .build());
      }else {
        if(admin.getTradetype().equals(RoleEnum.BUYER.name())) {
          adminMatrixDtosList.add(MatrixUserDto.builder()
                  .matrixUserNameFormat(adminUserName)
                  .userName(admin.getUserName())
                  .providerId(admin.getProviderId())
                  .providerUserId(admin.getUserId())
                  .category(constructCategoryList(requestDto.getMessage().getRequest().getItems()))
                  .role(RoleEnum.BUYER_ADMIN.name())
                  .nfcId(admin.getNfcId())
                  .build());
        }else if(admin.getTradetype().equals(RoleEnum.SELLER.name())){
          adminMatrixDtosList.add(MatrixUserDto.builder()
                  .matrixUserNameFormat(adminUserName)
                  .userName(admin.getUserName())
                  .providerId(admin.getProviderId())
                  .providerUserId(admin.getUserId())
                  .category(constructCategoryList(requestDto.getMessage().getRequest().getItems()))
                  .role(RoleEnum.BUYER_ADMIN.name())
                  .nfcId(admin.getNfcId())
                  .build());
        }
      }
    }
    return adminMatrixDtosList;
  }

   */

  private MatrixUserDto getBuyerMatrixUserDto(RequestDto requestDto) {
    String buyerUserName =
        String.format(
            "%s_%s",
            requestDto.getMessage().getRequest().getBuyer().getAppId(),
            requestDto.getMessage().getRequest().getBuyer().getUserId());

    return MatrixUserDto.builder()
        .matrixUserNameFormat(buyerUserName)
        .userName(requestDto.getMessage().getRequest().getBuyer().getName())
        .providerId(requestDto.getMessage().getRequest().getBuyer().getAppId())
        .providerUserId(requestDto.getMessage().getRequest().getBuyer().getUserId())
        .category(constructCategoryList(requestDto.getMessage().getRequest().getItems()))
        .role(RoleEnum.BUYER.name())
        .nfcId(requestDto.getMessage().getRequest().getBuyer().getNfcUserId())
        .build();
  }

  private MatrixUserDto getSellerMatrixUserDto(RequestDto requestDto) {
    String sellerUserName =
        String.format(
            "%s_%s",
            requestDto.getMessage().getRequest().getSeller().getAppId(),
            requestDto.getMessage().getRequest().getSeller().getUserId());

    return MatrixUserDto.builder()
        .matrixUserNameFormat(sellerUserName)
        .userName(requestDto.getMessage().getRequest().getSeller().getName())
        .providerId(requestDto.getMessage().getRequest().getSeller().getAppId())
        .providerUserId(requestDto.getMessage().getRequest().getSeller().getUserId())
        .category(constructCategoryList(requestDto.getMessage().getRequest().getItems()))
        .nfcId(requestDto.getMessage().getRequest().getSeller().getNfcUserId())
        .role(RoleEnum.SELLER.name())
        .build();
  }

  private List<String> constructCategoryList(List<ItemsDto> items) {
    List<String> cateogryList = new ArrayList<>();
    for (ItemsDto item : items) {
      cateogryList.add(item.getName());
    }
    return cateogryList;
  }

  public String forwardRequest(ClientRequestDto clientRequestDto) {
    checkAdminFlowMandatoryValues(clientRequestDto);
    List<RequestGroupDto> createdRequestGroup = getCreatedRequestGroup(clientRequestDto);
    for (SellerDto destinationUser : clientRequestDto.getDestinationUsers()) {
      Message message = setMessage(clientRequestDto, destinationUser, null);
      RequestDto requestDto = RequestDto.builder()
          .context(Context.builder().action("request_creation")
              .providerId(clientRequestDto.getProviderId())
              .transactionId("1")
              .build())
          .message(message)
          .requestGroupDto(createdRequestGroup.get(0))
          .build();
      RequestDto nfcResponse = requestNegotiation(requestDto);

//      ProviderUser providerUser = providerUserRepository.findFirstByProviderIdAndProviderUserIdAndDeleted(
//          destinationUser.getAppId(), destinationUser.getUserId(), 0);
//      String fcmToken = null;
//      if (Objects.nonNull(providerUser) && Objects.nonNull(providerUser.getFcmToken())) {
//        fcmToken = providerUser.getFcmToken();
//      }
//      if (fcmToken != null) {
//        PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
//            .title("Request to negotiate")
//            .message(clientRequestDto.getSourceUser().getName() + "wanted to negotiate with you")
//            .token(fcmToken)
//            .data("")
//            .url(getNfcChatUrl(nfcResponse))
//            .build();
//        fcmPushNotifier.sendMessageToToken(Arrays.asList(pushNotificationRequest));
//      }

    }
    return "request created successfully";
  }

  private void checkAdminFlowMandatoryValues(ClientRequestDto clientRequestDto) {
    if (!clientRequestDto.isAdminAssistedFlow()) {
      return;
    }

    InitiatedByDto adminInfo = clientRequestDto.getAdminInfo();

    if (adminInfo == null || adminInfo.getNfcUserId() == null) {
      throw CommonUtils.logAndGetException("Invalid input");
    }
  }

  private static Message setMessage(ClientRequestDto clientRequestDto, SellerDto destinationUser,
      List<RequestGroupDto> createdRequestGroup) {
    Message message = new Message();
    List<ItemsDto> itemsDtos = new ArrayList<>();
    for (ItemsDto dto : clientRequestDto.getItems()) {
      ItemsDto itemDto = ItemsDto.builder()
          .itemType(dto.getItemType())
          .id(dto.getId())
          .name(dto.getName())
          .price(dto.getPrice())
          .quantity(dto.getQuantity())
          .imageURL(dto.getImageURL())
          .category(dto.getCategory())

          .build();
      itemsDtos.add(itemDto);
    }
    RequestObjectDto requestObjectDto = RequestObjectDto.builder()
        .requestType(Integer.valueOf(clientRequestDto.getRequestType()))
        .items(itemsDtos)
        .message(clientRequestDto.getMessage())
        .adminInfo(clientRequestDto.getAdminInfo())
        .adminAssistedFlow(clientRequestDto.isAdminAssistedFlow())
        .build();
    if (clientRequestDto.getRequestType().equals(RequestType.BUY.getRequestType())) {
      requestObjectDto.setSeller(destinationUser);
      requestObjectDto.setBuyer(clientRequestDto.getSourceUser());
    } else if (clientRequestDto.getRequestType().equals(RequestType.SELL.getRequestType())) {
      requestObjectDto.setSeller(clientRequestDto.getSourceUser());
      requestObjectDto.setBuyer(destinationUser);
    }
    message.setRequest(requestObjectDto);
    return message;
  }

  private List<RequestGroupDto> getCreatedRequestGroup(ClientRequestDto clientRequestDto) {
    RequestGroupDto requestGroupDto = constructRequestGroupDtoV1(clientRequestDto);
    List<RequestGroupItems> items = constructAndAddItems(clientRequestDto);
    requestGroupDto.setItems(items);
    List<RequestGroupDto> createdRequestGroup = orderManagementRestService.createRequestGroup(
        requestGroupDto);
    if (CollectionUtils.isEmpty(createdRequestGroup)) {
      throw new RuntimeException("Something went wrong");
    }
    RequestGroupDto savedDto = createdRequestGroup.get(0);
    if (savedDto.getId() == null) {
      log.error("Request Group Id is null");
      throw new RuntimeException("Something went wrong");
    }
    return createdRequestGroup;
  }

  private RequestGroupDto constructRequestGroupDtoV1(ClientRequestDto clientRequestDto) {
    RequestGroupDto requestGroupDto =
        RequestGroupDto.builder()
            .requestType(clientRequestDto.getRequestType())
            .providerId(clientRequestDto.getProviderId())
            .status("RAISED")
            .providerUserId(clientRequestDto.getSourceUser().getUserId())
            .additionalDetails("test")
            .build();
    if (clientRequestDto.getAdminInfo() != null) {
      requestGroupDto.setProviderUserId(clientRequestDto.getAdminInfo().getUserId());
    }
    if (clientRequestDto.getDestinationUsers().size() == 1) {
      SellerDto user = clientRequestDto.getDestinationUsers().get(0);
      String name = user.getName();
      if (user.getAddress() != null) {
        String address = " ";
        if (!StringUtils.isEmpty(user.getAddress().getCity())) {
          address = address + user.getAddress().getCity() + ",";
        }
        if (!StringUtils.isEmpty(user.getAddress().getState())) {
          address = address + user.getAddress().getState();
        }
        if (!StringUtils.isEmpty(user.getAddress().getAreaCode())) {
          address = address + "-" + user.getAddress().getAreaCode();
        }
        requestGroupDto.setDisplayText(name + "\n" + address);
      }
    } else if (clientRequestDto.getDestinationUsers().size() >= 2) {
      requestGroupDto.setDisplayText("MULTIPLE USERS");
    } else if (clientRequestDto.getDestinationUsers().size() == 0) {
      requestGroupDto.setDisplayText("Match making by NFC");
    }
    return requestGroupDto;
  }

  private static List<RequestGroupItems> constructAndAddItems(ClientRequestDto clientRequestDto) {
    List<RequestGroupItems> items = new ArrayList<>();
    for (ItemsDto each : clientRequestDto.getItems()) {
      RequestGroupItems requestGroupItems =
          RequestGroupItems.builder()
              .itemId(each.getId())
              .itemName(each.getName())
              .price(each.getPrice().getValue())
              .quantity(each.getQuantity().getCount())
              .imageUrl(each.getImageURL())
              .build();
      items.add(requestGroupItems);
    }
    return items;
  }
}

