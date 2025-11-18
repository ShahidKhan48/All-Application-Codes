package com.ninjacart.nfcservice.service;

import static org.reflections.util.ConfigurationBuilder.build;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.configuration.DynamicPropertyHelper;

import com.ninjacart.nfcservice.dtos.AdditionalDetail;
import com.ninjacart.nfcservice.dtos.AdminResponseDto;
import com.ninjacart.nfcservice.dtos.CustomerRepresentativeDto;

import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.FreeFlowPartyDto;
import com.ninjacart.nfcservice.dtos.MatrixUserDto;
import com.ninjacart.nfcservice.dtos.LoginDto;
import com.ninjacart.nfcservice.dtos.MatrixUserAuthResponseDto;
import com.ninjacart.nfcservice.dtos.AdminResponseDto;
import com.ninjacart.nfcservice.dtos.ApprovalsDto;
import com.ninjacart.nfcservice.dtos.ProviderIdDto;
import com.ninjacart.nfcservice.dtos.UserRoleDto;
import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import com.ninjacart.nfcservice.dtos.MessageDto;
import com.ninjacart.nfcservice.dtos.OrderRequestMessageDto;
import com.ninjacart.nfcservice.dtos.TenantUserOnboardResponseDto;
import com.ninjacart.nfcservice.dtos.OnboardRequestDto;
import com.ninjacart.nfcservice.dtos.OnboardDto;
import com.ninjacart.nfcservice.dtos.OnboardResponseDto;
import com.ninjacart.nfcservice.dtos.CommandMessageBodyDto;
import com.ninjacart.nfcservice.dtos.WelcomeMessageDto;
import com.ninjacart.nfcservice.dtos.RoomCreationDto;
import com.ninjacart.nfcservice.dtos.FcmInfoDto;
import com.ninjacart.nfcservice.dtos.TenantChatUserDetails;
import com.ninjacart.nfcservice.dtos.RequestCreationResponse;
import com.ninjacart.nfcservice.dtos.FederationClass;
import com.ninjacart.nfcservice.dtos.RoomCreationResponse;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.dtos.request.RequestObjectDto;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import com.ninjacart.nfcservice.dtos.requestGroup.RequestGroupDto;
import com.ninjacart.nfcservice.dtos.requestGroup.RequestGroupItems;
import com.ninjacart.nfcservice.entity.Admin;
import com.ninjacart.nfcservice.entity.ProviderUser;
import com.ninjacart.nfcservice.enums.FreeFlowPartyTypeEnum;
import com.ninjacart.nfcservice.enums.RoleEnum;
import com.ninjacart.nfcservice.enums.MsgTypeEnum;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.enums.RoomPresetEnum;
import com.ninjacart.nfcservice.enums.RequestType;
import com.ninjacart.nfcservice.enums.RoomVisibilityEnum;
import com.ninjacart.nfcservice.enums.RequestGroupStatusEnum;
import com.ninjacart.nfcservice.enums.OrderRequestCommandStatusEnum;
import com.ninjacart.nfcservice.helpers.configuration.ApplicationConfiguration;
import com.ninjacart.nfcservice.repository.AdminRepository;
import com.ninjacart.nfcservice.repository.ProviderUserRepository;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProvideruserService {


    @Autowired
    private UsersService usersService;

    @Autowired
    private ProviderUserRepository providerUserRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MatrixRestService matrixRestService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private OrderManagementRestService orderManagementRestService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private WorkflowRestService workflowRestService;

    @Autowired private ApplicationConfiguration applicationConfiguration;

    @Autowired
    private OrderRequestCreationCommandService orderRequestCreationCommandService;
    @Autowired
    private InitiateChatCommandProcessorService initiateChatCommandProcessorService;
    private static final int RANDOM_NUMBER_GENERATOR_LENGTH = 10;

    private static final String MATRIX_PREFIX = ":95.216.170.148";
    private static final String MATRIX_USER_ID_PREFIX = "@";
    private static final String MATRIX_PREFIX_CONFIG_KEY = "matrix.user.prefix";
    private static final String DEFAULT_ROOM_TOPIC = "TRADE";

    private static final String ORDER_CREATION_ENTITY_STATUS = "ORDER_CREATION_REQUEST";
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


    public List<MatrixUserDto> onboardUsersOnMatrixIfNotPresent(List<MatrixUserDto> matrixUserDtoList)
            throws NoSuchAlgorithmException, InvalidKeyException {


        List<String> userIds = new ArrayList<>();
        log.debug("inside providerservice");
        List<String> providerId = new ArrayList<>();
        List<String> providerUserId = new ArrayList<>();
        for (MatrixUserDto matrixUserDto : matrixUserDtoList) {
            providerId.add(matrixUserDto.getProviderId());
            providerUserId.add(matrixUserDto.getProviderUserId());
        }
        // Get a List of all the ProviderUser in DB.
        List<ProviderUser> providerUserList =
                providerUserRepository.findByProviderIdInAndProviderUserIdIn(providerId, providerUserId);

        // convert the list to map with providerId+providerUserId being the key.
        Map<String, ProviderUser> idToUserMap =
                providerUserList.stream().collect(Collectors.toMap(e -> e.getProviderId() + "_" + e.getProviderUserId(), e -> e, (a, b) -> b));
        List<ProviderUser> newUsersList = new ArrayList<>();
        Map<String, ProviderUser> toOnboardMap = new HashMap<>();

        // Iterate the Matrix List
        for (MatrixUserDto matrixUser : matrixUserDtoList) {
            ProviderUser providerUser = new ProviderUser();
            ProviderUser existingUser = idToUserMap.get(matrixUser.getProviderId() + "_" + matrixUser.getProviderUserId());
            if (existingUser == null) {
                log.debug("user in not present for providerId_providerUserId" + matrixUser.getProviderId() + "_" + matrixUser.getProviderUserId());
                providerUser.setProviderId(matrixUser.getProviderId());
                providerUser.setProviderUserId(matrixUser.getProviderUserId());
                providerUser.setUserName(matrixUser.getUserName());
                providerUser.setPassword(matrixUser.getPassword());
                providerUser.setCategory(getCsvCategories(matrixUser.getCategory()));
                newUsersList.add(providerUser);
                toOnboardMap.put(matrixUser.getProviderId() + "_" + matrixUser.getProviderUserId(), providerUser);
                // add to list and onboard.
            } else if (existingUser != null && StringUtils.isEmpty(existingUser.getToken())) {
                log.debug("Token is empty for user checking if username available " + matrixUser.getProviderId() + "_" + matrixUser.getProviderUserId());
                log.debug(matrixUser.getProviderId() + "_" + matrixUser.getProviderUserId());
                if (matrixRestService.checkUserNameAvailability(matrixFormatedUserId(matrixUser.getProviderId() + "_" + matrixUser.getProviderUserId()))) {
                    log.debug("UserName is available onboarding user");
                    toOnboardMap.put(matrixUser.getProviderId() + "_" + matrixUser.getProviderUserId(), providerUser);
                    newUsersList.add(existingUser);
                    //add to list and onboard
                } else {
                    log.debug("sending login dto as " + LoginDto.builder().user(existingUser.getProviderId() + "_" + existingUser.getProviderUserId()).password(existingUser.getPassword()).build());
                    OnboardResponseDto responseDto = matrixRestService.login(LoginDto.builder().user(existingUser.getProviderId() + "_" + existingUser.getProviderUserId()).password(existingUser.getPassword()).build());
                    log.debug("User present in database logging in {}" + responseDto);
                    matrixUser.setMatrixToken(responseDto.getAccessToken());
                    matrixUser.setMatrixDeviceId(responseDto.getDeviceId());
                    matrixUser.setMatrixUserId(responseDto.getUserId());
                    matrixUser.setNfcId(existingUser.getId());
                    newUsersList.add(updateFields(existingUser, responseDto));// save upadted data again in db using list
                    userIds.add(existingUser.getChatUserId());
                }
            } else {
                matrixUser.setMatrixUserId(existingUser.getChatUserId());
                matrixUser.setMatrixToken(existingUser.getToken());
                matrixUser.setMatrixDeviceId(existingUser.getDeviceId());
                matrixUser.setNfcId(existingUser.getId());
            }
        }
        if (!CollectionUtils.isEmpty(newUsersList)) {
            saveList(newUsersList);
        }

        for (MatrixUserDto matrixUser : matrixUserDtoList) {
            ProviderUser existingUser = toOnboardMap.get(matrixUser.getProviderId() + "_" + matrixUser.getProviderUserId());
            OnboardResponseDto responseDto = new OnboardResponseDto();
            if (existingUser == null) {
                continue;
            } else if (StringUtils.isEmpty(existingUser.getToken())) {
                OnboardDto onboardDto = constructOnboardDto(matrixUser);
                existingUser.setPassword(onboardDto.getPassword());
                matrixUser.setNfcId(existingUser.getId());
                matrixUser.setPassword(onboardDto.getPassword());
                responseDto = usersService.onboardUser(onboardDto);
                matrixUser.setMatrixToken(responseDto.getAccessToken());
                matrixUser.setMatrixDeviceId(responseDto.getDeviceId());
                matrixUser.setMatrixUserId(responseDto.getUserId());
            }
            matrixUser.setNfcId(existingUser.getId());
            updateFields(existingUser, responseDto);
            userIds.add(existingUser.getChatUserId());
        }
        List<ProviderUser> updatedUsersList = toOnboardMap.values().stream()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(updatedUsersList)) {
            saveList(updatedUsersList);
        }
        return matrixUserDtoList;
    }

    private static String getMatrixPrefixId() {

        return DynamicPropertyHelper.getValue(MATRIX_PREFIX_CONFIG_KEY, MATRIX_PREFIX);
    }

    public String matrixFormatedUserId(String userName) {
        String matrixUserPrefix = getMatrixPrefixId();
        StringBuilder formattedName =
                new StringBuilder(MATRIX_USER_ID_PREFIX).append(userName).append(matrixUserPrefix);
        return formattedName.toString();
    }

    private String getCsvCategories(List<String> category) {
        if(CollectionUtils.isEmpty(category)) {
            return "";
        }
        return String.join(",",category);

    }

    @Transactional
    public void saveList(List<ProviderUser> newUsersList) {

        providerUserRepository.saveAll(newUsersList);
    }


    private ProviderUser updateFields(ProviderUser existingUser, OnboardResponseDto responseDto) {
        existingUser.setToken(responseDto.getAccessToken());
        existingUser.setDeviceId(responseDto.getDeviceId());
        existingUser.setChatUserId(responseDto.getUserId());
        return existingUser;
    }

    private OnboardDto constructOnboardDto(MatrixUserDto matrixUserDto) {
        return OnboardDto.builder()
                .admin(matrixUserDto.isMatrixChatAdmin())
                .displayname(matrixUserDto.getUserName())
                .username(matrixUserDto.getMatrixUserNameFormat())
                .password(UUID.randomUUID().toString())
                .build();
    }

    public TenantUserOnboardResponseDto onboardTenant(OnboardRequestDto onboardRequestDto)
            throws Exception {
        MatrixUserDto matrixUserDto = constructMatrixUserDto(onboardRequestDto);
        List<MatrixUserDto> matrixUserDtos =
                onboardUsersOnMatrixIfNotPresent(Arrays.asList(matrixUserDto));
        if (CollectionUtils.isEmpty(matrixUserDtos)) {
            throw CommonUtils.logAndGetException("Something went wrong");
        }
        return constructTenantOnboardResponse(matrixUserDtos.get(0), onboardRequestDto);
    }

    private MatrixUserDto constructMatrixUserDto(OnboardRequestDto onboardRequestDto) {
        return MatrixUserDto.builder()
                .providerUserId(onboardRequestDto.getMessage().getUserInfo().getUserId())
                .userName(onboardRequestDto.getMessage().getUserInfo().getUserName())
                .category(onboardRequestDto.getMessage().getUserInfo().getCategories())
                .matrixUserNameFormat(
                        String.format(
                                "%s_%s",
                                onboardRequestDto.getContext().getProviderId(),
                                onboardRequestDto.getMessage().getUserInfo().getUserId()))
                .providerId(onboardRequestDto.getContext().getProviderId())
                .password(UUID.randomUUID().toString())
                .build();
    }

    private TenantUserOnboardResponseDto constructTenantOnboardResponse(
            MatrixUserDto matrixUserDto, OnboardRequestDto onboardRequestDto) {
        return TenantUserOnboardResponseDto.builder()
                .context(onboardRequestDto.getContext())
                .tenantChatUserDetails(
                        TenantChatUserDetails.builder()
                                .chatUserId(matrixUserDto.getMatrixUserId())
                                .chatDeviceId(matrixUserDto.getMatrixDeviceId())
                                .chatToken(matrixUserDto.getMatrixToken())
                                .nfcUserId(
                                        matrixUserDto.getNfcId() != null ? matrixUserDto.getNfcId().toString() : null)
                                .build())
                .build();
    }

    public List<ProviderUser> findByIds(List<Integer> providerUserIds) {
        return providerUserRepository.findAllById(providerUserIds);
    }

    private MatrixUserAuthResponseDto constructMatrixUserAuthResponseDto(
            ProviderUser providerUser) {
        return MatrixUserAuthResponseDto.builder()
                .deviceId(providerUser.getDeviceId())
                .token(providerUser.getToken())
                .chatUserId(providerUser.getChatUserId())
                .providerUserId(providerUser.getProviderUserId())
                .providerId(providerUser.getProviderId())
                .nfcUserId(providerUser.getId())
                .build();
    }

    public MatrixUserAuthResponseDto getMatrixAuthDetails(Integer nfcUserId) {
        Optional<ProviderUser> providerUser = providerUserRepository.findById(nfcUserId);
        if (!providerUser.isPresent()) {
            throw CommonUtils.logAndGetException("User not onboarded");
        }
        return constructMatrixUserAuthResponseDto(providerUser.get());
    }


  public List<String> getProviderIds(List<MatrixUserDto> matrixUserDtoList,String role) {
    MatrixUserDto roleMaxtrix = null;

    for (MatrixUserDto each : matrixUserDtoList) {
      if (each.getRole().equals(role)) {
        roleMaxtrix = each;
      }

    }

    if (roleMaxtrix == null) {
      throw CommonUtils.logAndGetException("Something went wrong");
    }

    List<String> providerIdList = new ArrayList<>();

      providerIdList = Arrays.asList(roleMaxtrix.getProviderId());

    return providerIdList;
  }
    public ProviderIdDto getProviderIdDto(List<MatrixUserDto> matrixUserDtoList) {
        MatrixUserDto roleMaxtrix = null;
        ProviderIdDto providerIdDto = new ProviderIdDto();
        for (MatrixUserDto each : matrixUserDtoList) {
            if (each.getRole().equals(RoleEnum.BUYER.name())) {
                providerIdDto.setBuyerProviderId(each.getProviderId());

            }
            if (each.getRole().equals(RoleEnum.SELLER.name())) {
                providerIdDto.setSellerProviderId(each.getProviderId());

            }

        }
        return providerIdDto;

    }

    public List<MatrixUserDto> getAdminChatUserIds(List<MatrixUserDto> matrixUserDtoList) {
        MatrixUserDto buyerMaxtrix = null;
        MatrixUserDto sellerMatrix = null;
        for (MatrixUserDto each : matrixUserDtoList) {
            if (each.getRole().equals(RoleEnum.BUYER.name())) {
                buyerMaxtrix = each;
            }
            if (each.getRole().equals(RoleEnum.SELLER.name())) {
                sellerMatrix = each;
            }
        }

        if (buyerMaxtrix == null || sellerMatrix == null) {
            throw CommonUtils.logAndGetException("Something went wrong");
        }

        List<String> providerIdList = new ArrayList<>();
        if (buyerMaxtrix.getProviderId().equals(sellerMatrix.getProviderId())) {
            providerIdList = Arrays.asList(buyerMaxtrix.getProviderId());
        } else {
            providerIdList = Arrays.asList(buyerMaxtrix.getProviderId(), sellerMatrix.getProviderId());
        }

        List<Admin> adminList = adminRepository.findByProviderIdInAndActiveTrue(providerIdList);
        if (CollectionUtils.isEmpty(adminList)) {
            return new ArrayList<>();
        }
        Map<String, String> providerToTraderTypeToUserMap = getProviderToTraderTypeToUserMap(adminList);
        List<MatrixUserDto> adminMatrixUserDtos = getFilteredAdmins(providerToTraderTypeToUserMap, buyerMaxtrix, sellerMatrix);
        return adminMatrixUserDtos;
    }

    private Map<String, String> getProviderToTraderTypeToUserMap(List<Admin> adminList) {
        return adminList.stream().filter(e -> !StringUtils.isEmpty(e.getTradeType()) && !StringUtils.isEmpty(e.getTradeType())).collect(Collectors.toMap(e -> e.getProviderId() + "_" + e.getTradeType(), e -> e.getUserId(), (a, b) -> a));
    }

    private List<MatrixUserDto> getFilteredAdmins(
            Map<String, String> providerToTraderTypeToUserMap,
            MatrixUserDto buyerMaxtrix,
            MatrixUserDto sellerMatrix) {
        List<String> adminUserIds = new ArrayList<>();

        String buyerUserId =
                providerToTraderTypeToUserMap.get(
                        buyerMaxtrix.getProviderId() + "_" + RequestType.BUY.name());
        String sellerUserId =
                providerToTraderTypeToUserMap.get(
                        sellerMatrix.getProviderId() + "_" + RequestType.SELL.name());
        //        String matrixAdminUserId =
        // providerToTraderTypeToUserMap.get(CommonConstants.DEFAULT_PROVIDER_ID);

        if (!StringUtils.isEmpty(buyerUserId)) {
            adminUserIds.add(buyerUserId);
        }
        if (!StringUtils.isEmpty(sellerUserId)) {
            adminUserIds.add(sellerUserId);
        }
        //        if(matrixAdminUserId!=null){
        //            adminUserIds.add(matrixAdminUserId);
        //        }

        if (CollectionUtils.isEmpty(adminUserIds)) {
            return new ArrayList<>();
        }

        List<String> providerIds = getProviderIds(buyerMaxtrix, sellerMatrix);

        List<ProviderUser> providerUsers =
                providerUserRepository.findByProviderIdInAndProviderUserIdInAndDeleted(
                        providerIds, adminUserIds, 0);

        if (CollectionUtils.isEmpty(providerIds)) {
            return new ArrayList<>();
        }

        List<MatrixUserDto> adminMatrixUserDtos = new ArrayList<>();

        for (ProviderUser each : providerUsers) {

            if (StringUtils.isEmpty(each.getChatUserId())) {
                continue;
            }

            if (each.getProviderId().equals(buyerMaxtrix.getProviderId())
                    && each.getProviderUserId().equals(buyerUserId)) {
                adminMatrixUserDtos.add(
                        MatrixUserDto.builder()
                                .role(
                                        checkIfBothAdminsAreSame(buyerUserId, sellerUserId)
                                                ? RoleEnum.SELLER_BUYER_ADMIN.name()
                                                : RoleEnum.BUYER_ADMIN.name())
                                .userName(each.getUserName())
                                .providerId(each.getProviderId())
                                .providerUserId(each.getProviderUserId())
                                .nfcId(each.getId())
                                .matrixUserId(each.getChatUserId())
                                .build());
            } else if (each.getProviderId().equals(sellerMatrix.getProviderId())
                    && each.getProviderUserId().equals(sellerUserId)) {
                adminMatrixUserDtos.add(
                        MatrixUserDto.builder()
                                .role(
                                        checkIfBothAdminsAreSame(buyerUserId, sellerUserId)
                                                ? RoleEnum.SELLER_BUYER_ADMIN.name()
                                                : RoleEnum.SELLER_ADMIN.name())
                                .userName(each.getUserName())
                                .providerId(each.getProviderId())
                                .providerUserId(each.getProviderUserId())
                                .nfcId(each.getId())
                                .matrixUserId(each.getChatUserId())
                                .build());
            }
        }

        return adminMatrixUserDtos;
    }

    private boolean checkIfBothAdminsAreSame(String buyerUserId, String sellerUserId) {
        if (StringUtils.isEmpty(buyerUserId) || StringUtils.isEmpty(sellerUserId)) {
            return false;
        }
        return buyerUserId.equals(sellerUserId);
    }

    private List<String> getProviderIds(MatrixUserDto buyerMatrixDto, MatrixUserDto sellerMatrix) {
        List<String> providerIds = new ArrayList<>();
        providerIds.add(buyerMatrixDto.getProviderId());
        providerIds.add(sellerMatrix.getProviderId());
        //        providerIds.add(CommonConstants.DEFAULT_PROVIDER_ID);

        return providerIds;
    }

    public AdminResponseDto fetchAdminDetails(String providerId, String providerUserId) {
        if (StringUtils.isEmpty(providerId)) {
            throw CommonUtils.logAndGetException("Invalid providerId");
        }
        if (StringUtils.isEmpty(providerUserId)) {
            throw CommonUtils.logAndGetException("Invalid providerUserId");
        }

        Admin admin =
                adminRepository.findFirstByProviderIdAndUserIdAndActiveTrue(providerId, providerUserId);
        AdminResponseDto adminResponseDto = null;
        if (admin == null) {
            return AdminResponseDto.builder().admin(false).build();
        } else {
            adminResponseDto =
                    AdminResponseDto.builder().admin(true).tradeType(admin.getTradeType()).build();
        }

        ProviderUser providerUser =
                providerUserRepository.findFirstByProviderIdAndProviderUserIdAndDeleted(
                        providerId, providerUserId, 0);

        if (providerUser != null) {
            adminResponseDto.setOnboarded(true);
            adminResponseDto.setNfcUserId(providerUser.getId());
        }

        return adminResponseDto;
    }

    public ProviderUser findByIdWithDeletedFalse(Integer id) {
        return providerUserRepository.findFirstByIdAndDeleted(id, 0);
    }

    public List<ProviderUser> findByIdsWithDeletedFalse(List<Integer> ids) {
        return providerUserRepository.findAllByIdInAndDeleted(ids, 0);
    }

    public RequestDto createOrder(RequestDto requestDto) {
        log.debug("requestDto : {}", requestDto);

        RequestGroupDto requestGroupDto = requestDto.getRequestGroupDto();
        if (requestGroupDto == null) {
            requestGroupDto = createRequestGroup(requestDto);
        }
        List<MatrixUserDto> matrixUserDtoList = getUserNames(requestDto);
        if (CollectionUtils.isEmpty(matrixUserDtoList)) {
            throw CommonUtils.logAndGetException("Something went wrong");
        }
        List<Integer> nfcUserIds = matrixUserDtoList.stream().filter(e -> e.getNfcId() != null).map(e -> e.getNfcId()).collect(Collectors.toList());
        nfcUserIds.add(requestDto.getMessage().getRequest().getAdminInfo().getNfcUserId());
        Set<String> chatUserIds = getChatUserIds(nfcUserIds);
        List<MatrixUserDto> adminUserDtos = getAdminChatUserIds(matrixUserDtoList);
        List<String> adminChatUserIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(adminUserDtos)) {
            adminChatUserIds = adminUserDtos.stream().filter(e -> !StringUtils.isEmpty(e.getMatrixUserId())).map(e -> e.getMatrixUserId()).collect(Collectors.toList());
            matrixUserDtoList.addAll(adminUserDtos);
        }
        if (!CollectionUtils.isEmpty(adminChatUserIds)) {
            chatUserIds.addAll(adminChatUserIds);
        }
        RoomCreationResponse roomCreationResponse =
                constructDtoAndCreateChatRoom(chatUserIds, requestDto);
        List<FreeFlowPartyDto> freeFlowPartyDtos=constructPartyDto(Collections.emptySet(),requestDto,adminUserDtos,false);
        requestDto.getMessage().getRequest().setChatRoomId(roomCreationResponse.getRoomId());
        List<UserRoleDto> userRoleDtos = constructUserRoleDtos(matrixUserDtoList);
        List<ApprovalsDto> approvals =
                constructApprovalsForRequestNegotiation1(
                        matrixUserDtoList, InitiatedByDto.builder().build());
        RequestCreationResponse requestCreationResponse = createRequests(requestDto, requestGroupDto, userRoleDtos, approvals,freeFlowPartyDtos);
        triggerWelcomeMessage(roomCreationResponse.getRoomId());
        triggerOrderRequestApprovalCommand( requestDto.getMessage().getRequest(), requestCreationResponse, requestDto.getMessage().getRequest().getAdminInfo());
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
                freeFlowPartyDto.setNfcUserId(Integer.parseInt(optionalNfcUserId.get().getRefValue()));
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

        FreeFlowPartyDto cRDto=FreeFlowPartyDto.builder().nfcUserId(requestDto.getMessage().getRequest().getAdminInfo().getNfcUserId())
            .partyType(FreeFlowPartyTypeEnum.CR.getId())
            .name(requestDto.getMessage().getRequest().getAdminInfo().getName())
            .email(requestDto.getMessage().getRequest().getAdminInfo().getEmail())
            .phone(requestDto.getMessage().getRequest().getAdminInfo().getPhone()).build();
        freeFlowPartyDtos.add(cRDto);

        FreeFlowPartyDto defaultAdminDto = FreeFlowPartyDto.builder()
            .nfcUserId(applicationConfiguration.getNfcAdminUserId())
            .partyType(FreeFlowPartyTypeEnum.ADMIN.getId()).build();
        freeFlowPartyDtos.add(defaultAdminDto);
        if (!adminUserDtos.isEmpty()&&isFallbackAdmin) {
            FreeFlowPartyDto fallbackAdminDto = FreeFlowPartyDto.builder()
                .name(adminUserDtos.get(0).getUserName())
                .partyType(3).nfcUserId(adminUserDtos.get(0).getNfcId()).build();
            freeFlowPartyDtos.add(fallbackAdminDto);
        }
        return freeFlowPartyDtos;
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
                        .paymentTerms(requestObjectDto.getPaymentTerms())
                        .qualityTerms(requestObjectDto.getQualityTerms())
                        .build();

        MessageDto messageDto = MessageDto.builder().msgtype(MsgTypeEnum.COMMAND.getType()).build();

        CommandMessageBodyDto commandMessageBodyDto =
                CommandMessageBodyDto.builder()
                        .command(CommandTypeEnum.ORDER_REQUEST_APPROVAL.name())
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


    private List<ApprovalsDto> constructApprovalsForRequestNegotiation1(List<MatrixUserDto> matrixUserDtoList, InitiatedByDto initiated) {
        List<ApprovalsDto> approvals = new ArrayList<>();
        for (MatrixUserDto each : matrixUserDtoList) {
            if (each.getRole().equals(RoleEnum.BUYER.name()) || each.getRole().equals(RoleEnum.SELLER.name())) {
                ApprovalsDto approvalsDto = ApprovalsDto.builder()
                        .role(each.getRole())
                        .providerId(each.getProviderId())
                        .userId(each.getProviderUserId())
                        .nfcUserId(each.getNfcId())
                        .status(each.getMatrixUserId())
                        .build();
                approvalsDto.setStatus(OrderRequestCommandStatusEnum.PENDING.name());
                approvals.add(approvalsDto);
            }
        }

        return approvals;
    }
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
    private RequestGroupDto constructRequestGroupDto(RequestDto requestDto) {
        RequestGroupDto requestGroupDto =
                RequestGroupDto.builder()
                        .requestType(requestDto.getMessage().getRequest().getRequestType().toString())
                        .providerId(requestDto.getContext().getProviderId())
                        .status(RequestGroupStatusEnum.RAISED.name())
                        .message(requestDto.getMessage().getRequest().getMessage())
                        .build();

        if (requestGroupDto.getRequestType().equals(RequestType.BUY.getRequestType())) {
            requestGroupDto.setProviderUserId(
                    requestDto.getMessage().getRequest().getBuyer().getUserId());
        } else if (requestGroupDto.getRequestType().equals(RequestType.SELL.getRequestType())) {
            requestGroupDto.setProviderUserId(
                    requestDto.getMessage().getRequest().getSeller().getUserId());
        }

        return requestGroupDto;
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
    private List<MatrixUserDto> getUserNames(RequestDto requestDto) {
        List<MatrixUserDto> matrixUserDtoList = new ArrayList<>();
        MatrixUserDto buyerMaxtrix = getBuyerMatrixUserDto(requestDto);
        MatrixUserDto sellerMatrix = getSellerMatrixUserDto(requestDto);
        matrixUserDtoList.add(buyerMaxtrix);
        matrixUserDtoList.add(sellerMatrix);
        return matrixUserDtoList;
    }

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

    private Set<String> getChatUserIds(List<Integer> nfcUserIds) {
        List<ProviderUser> providerUsers = findByIds(nfcUserIds);
        if (CollectionUtils.isEmpty(nfcUserIds)) {
            throw CommonUtils.logAndGetException("Invalid UserIds");
        }
        return providerUsers.stream().filter(e -> !StringUtils.isEmpty(e.getChatUserId())).map(e -> e.getChatUserId()).collect(Collectors.toSet());
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

    public RequestCreationResponse createRequests(
            RequestDto requestDto, RequestGroupDto requestGroupDto, List<UserRoleDto> userRoleDtos, List<ApprovalsDto> approvals,
    List<FreeFlowPartyDto> freeFlowPartyDtos) {
        RequestObjectDto requestObjectDto = requestDto.getMessage().getRequest();
        requestObjectDto.setRequestGroupId(requestGroupDto.getId().toString());
        requestObjectDto.setOwnerId(CommonConstants.DEFAULT_USERID);
        requestObjectDto.setUserRoleDtos(userRoleDtos);
        requestObjectDto.setApprovals(approvals);
        requestObjectDto.setCopyIdToExternalReferenceId(true);
        requestObjectDto.setEntityVersion(0);
        requestObjectDto.setEntityStatus(ORDER_CREATION_ENTITY_STATUS);
        requestObjectDto.setActive(true);
        requestObjectDto.setFreeFlowPartyDtos(freeFlowPartyDtos);
        Integer initiatedNfcUserId = requestObjectDto.getAdminInfo().getNfcUserId();
        RequestCreationResponse requestCreationResponse = workflowRestService.createRequest(requestDto.getMessage(), initiatedNfcUserId);
        return requestCreationResponse;
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
    private void triggerNegotiationRequestCommand(
            RequestObjectDto requestObjectDto, List<UserRoleDto> userRoleDtos, List<ApprovalsDto> approvals, RequestCreationResponse requestCreationResponse) {

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
    private String generateMatrixUniqueTransactionId() {
        return String.format(
                "%s.%s.%s", "m", new Date().getTime(), CommonUtils.randomNumberGenerator(10));
    }



  public List<MatrixUserDto> getAdminMatrixUserDtos(List<String> providerIds,
      List<String> adminUserIds) {
    List<ProviderUser> providerUsers = providerUserRepository.findByProviderIdInAndProviderUserIdInAndDeleted(
        providerIds, adminUserIds, 0);

    if (CollectionUtils.isEmpty(providerIds)) {
      return new ArrayList<>();
    }

    List<MatrixUserDto> adminMatrixUserDtos = new ArrayList<>();
    for (ProviderUser each : providerUsers) {

      if (StringUtils.isEmpty(each.getChatUserId())) {
        continue;
      }

      adminMatrixUserDtos.add(MatrixUserDto.builder().role("CUSTOMERREPRESENTATIVE").userName(each.getUserName())
          .providerId(each.getProviderId()).providerUserId(each.getProviderUserId())
          .nfcId(each.getId()).matrixUserId(each.getChatUserId()).build());
    }

    return adminMatrixUserDtos;
  }

    public List<MatrixUserDto> getDefaultAdminMatrixUserDto(String nfcuserId
    ) {
        int id=Integer.parseInt(nfcuserId);
        ProviderUser providerUser = providerUserRepository.findByIdAndDeleted(id, 0);

        List<MatrixUserDto> adminMatrixUserDtos = new ArrayList<>();

        adminMatrixUserDtos.add(
            MatrixUserDto.builder().role("CUSTOMERREPRESENTATIVE").userName(providerUser.getUserName())
                .providerId(providerUser.getProviderId())
                .providerUserId(providerUser.getProviderUserId())
                .nfcId(providerUser.getId()).matrixUserId(providerUser.getChatUserId()).build());

        return adminMatrixUserDtos;
    }




      public String getFcmId(String providerId,String providerUserId){
          ProviderUser providerUser = providerUserRepository.findFirstByProviderIdAndProviderUserIdAndDeleted(
              providerId, providerUserId, 0);
          if (providerUser == null) {
              return "";
          }
          return providerUser.getFcmToken();
      }
    public ProviderUser updateFcmId(FcmInfoDto fcmInfoDto) throws JsonProcessingException {

        if(StringUtils.isEmpty(fcmInfoDto.getProviderUserId())){
            throw CommonUtils.logAndGetException("providerUserId is null");
        }
        if(StringUtils.isEmpty(fcmInfoDto.getProviderId())){
            throw CommonUtils.logAndGetException("providerId is null");
        }
        ProviderUser providerUser = providerUserRepository.findFirstByProviderIdAndProviderUserIdAndDeleted(fcmInfoDto.getProviderId(), fcmInfoDto.getProviderUserId(),0);
        if(providerUser == null){
            throw CommonUtils.logAndGetException("providerUser is null");
        }
        providerUser.setFcmToken(fcmInfoDto.getFcmId());
        providerUserRepository.save(providerUser);
        return  providerUser;


    }

}
