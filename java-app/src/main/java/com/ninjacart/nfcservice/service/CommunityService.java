package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.*;
import com.ninjacart.nfcservice.dtos.CommunityAddressDto;
import com.ninjacart.nfcservice.dtos.CommunityContactObject;
import com.ninjacart.nfcservice.dtos.CommunityMemberDto;
import com.ninjacart.nfcservice.dtos.CommunityMemberWrapperDto;
import com.ninjacart.nfcservice.dtos.GemsEntityResponseDto;
import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import com.ninjacart.nfcservice.dtos.JoinUserDto;
import com.ninjacart.nfcservice.dtos.MembersObject;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import com.ninjacart.nfcservice.entity.ProviderUser;
import com.ninjacart.nfcservice.enums.RoomPresetEnum;
import com.ninjacart.nfcservice.enums.RoomVisibilityEnum;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommunityService {

  @Autowired private GemsRestService gemsRestService;

  @Autowired private ProvideruserService provideruserService;

  @Autowired private RoomService roomService;

  @Autowired private MatrixRestService matrixRestService;

  private static final String COMMUNITY_ENTITY = "Community";

  private static final String DEFAULT_ROOM_TOPIC = "Community-trading";

  private static final String DEFAULT_LEAVE_ROOM_REASON = "User wants to leave group";


  public GemsEntityResponseDto addMember(CommunityMemberWrapperDto communityMemberWrapperDto) {

    addMemberMandatoryValuesCheck(communityMemberWrapperDto);

    CommunityMemberDto communityMemberDto = communityMemberWrapperDto.getRequest();

    GemsEntityResponseDto gemsEntityResponseDto =
        gemsRestService.findGemsEntityById(communityMemberDto.getCommunityId(), CommonConstants.DEFAULT_REALM, String.valueOf(CommonConstants.DEFAULT_USERID), COMMUNITY_ENTITY);

    if (gemsEntityResponseDto == null) {
      throw CommonUtils.logAndGetException("Invalid Community");
    }

    checkIfInitiatedUserIsAdmin(gemsEntityResponseDto, communityMemberDto.getInitiatedByDto());

    boolean userPresent =
        checkIfUserIdIsAlreadyPartOfMembers(
            gemsEntityResponseDto, communityMemberDto.getMemberDto());

    if (!userPresent) {
      partyCommunityValidation(gemsEntityResponseDto);
      gemsEntityResponseDto = addMemberToEntity(gemsEntityResponseDto, communityMemberDto.getMemberDto());
      saveUpdateEntity(communityMemberDto, gemsEntityResponseDto);
    }

    joinMemberToRoom(gemsEntityResponseDto.getCommunity().getChatRoomId(), communityMemberDto.getMemberDto().getNfcUserId());

    return gemsEntityResponseDto;

  }

  private void partyCommunityValidation(GemsEntityResponseDto gemsEntityResponseDto) {
    if(CollectionUtils.isEmpty(gemsEntityResponseDto.getCommunity().getMembers())) {
      return;
    }

    if(gemsEntityResponseDto.getCommunity().getPartyCommunity()!=null && gemsEntityResponseDto.getCommunity().getPartyCommunity() && gemsEntityResponseDto.getCommunity().getMembers().size()>=2) {
      throw CommonUtils.logAndGetException("You cannot add more people in this personal community");
    }
  }

  private GemsEntityResponseDto addMemberToEntity(
      GemsEntityResponseDto gemsEntityResponseDto, SellerDto userDto) {
    MembersObject membersObject = convertUserDtoToMembersObject(userDto);
    gemsEntityResponseDto.getCommunity().getMembers().add(membersObject);
    return gemsEntityResponseDto;
  }

  private MembersObject convertUserDtoToMembersObject(SellerDto userDto) {
    return MembersObject.builder()
        .contact(
            CommunityContactObject.builder()
                .contactNumber(Arrays.asList(userDto.getPhone()))
                .build())
        .nfcUserId(userDto.getNfcUserId().toString())
        .userId(userDto.getUserId())
        .name(userDto.getName())
        .address(
            CommunityAddressDto.builder()
                .district(userDto.getAddress().getCity())
                .pincode(userDto.getAddress().getAreaCode())
                .locality(userDto.getAddress().getLocality())
                .state(userDto.getAddress().getState())
                .village(userDto.getAddress().getCity())
                .plot(userDto.getAddress().getDoor())
                .build())
        .build();
  }

  public String getMatrixUserId(Integer nfcUserId) {
    ProviderUser providerUser = provideruserService.findByIdWithDeletedFalse(nfcUserId);

    if(providerUser == null) {
      throw CommonUtils.logAndGetException("No user found");
    }

    if(StringUtils.isEmpty(providerUser.getChatUserId())) {
      throw CommonUtils.logAndGetException("User is not onboarded");
    }

    return providerUser.getChatUserId();
  }

  private List<String> getMatrixUserIds(List<Integer> nfcUserIds){
    List<ProviderUser> providerUsers = provideruserService.findByIdsWithDeletedFalse(nfcUserIds);
    if(CollectionUtils.isEmpty(providerUsers)) {
      throw CommonUtils.logAndGetException("users not found");
    }
    List<String> chatUserIds = new ArrayList<>();
    for(ProviderUser providerUser : providerUsers){
      String chatUserId = providerUser.getChatUserId();
      if(StringUtils.isEmpty(providerUser.getChatUserId())) {
        throw CommonUtils.logAndGetException("User is not onboarded");
      }
      chatUserIds.add(chatUserId);
    }
    return chatUserIds;
  }

  public void joinMemberToRoom(String roomId, Integer nfcUserId) {
    String chatUserId = getMatrixUserId(nfcUserId);
    roomService.joinAnUserToRoom(JoinUserDto
            .builder()
            .userId(chatUserId)
            .build(), roomId);
  }

  private void saveUpdateEntity(CommunityMemberDto communityMemberDto, GemsEntityResponseDto gemsEntityResponseDto) {
    gemsRestService.updateGemsEntityById(communityMemberDto.getCommunityId(), CommonConstants.DEFAULT_REALM, String.valueOf(CommonConstants.DEFAULT_USERID), COMMUNITY_ENTITY, gemsEntityResponseDto);
  }

  private void checkIfInitiatedUserIsAdmin(GemsEntityResponseDto gemsEntityResponseDto, InitiatedByDto initiatedByDto) {
    if (CollectionUtils.isEmpty(gemsEntityResponseDto.getCommunity().getMembers())) {
      return;
    }
    List<String> adminNfcUserIds =  gemsEntityResponseDto.getCommunity().getMembers().stream()
            .filter(e -> !StringUtils.isEmpty(e.getNfcUserId()) && e.getAdmin()!=null && e.getAdmin())
            .map(e -> e.getNfcUserId())
            .collect(Collectors.toList());

    if(!adminNfcUserIds.contains(initiatedByDto.getNfcUserId().toString())) {
      throw CommonUtils.logAndGetException("You are not admin of this group");
    }
  }

  private boolean checkIfUserIdIsAlreadyPartOfMembers(
      GemsEntityResponseDto gemsEntityResponseDto, SellerDto memberDto) {
    if (CollectionUtils.isEmpty(gemsEntityResponseDto.getCommunity().getMembers())) {
      return false;
    }
    List<String> communityNfcUserIds =
        gemsEntityResponseDto.getCommunity().getMembers().stream()
            .filter(e -> !StringUtils.isEmpty(e.getNfcUserId()))
            .map(e -> e.getNfcUserId())
            .collect(Collectors.toList());

    return communityNfcUserIds.contains(memberDto.getNfcUserId().toString());
  }

  private void addMemberMandatoryValuesCheck(CommunityMemberWrapperDto communityMemberWrapperDto) {
    if (communityMemberWrapperDto == null
        || communityMemberWrapperDto.getContext() == null
        || communityMemberWrapperDto.getRequest() == null) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }

    InitiatedByDto initiatedByDto = communityMemberWrapperDto.getRequest().getInitiatedByDto();

    if(initiatedByDto == null || initiatedByDto.getNfcUserId() == null || StringUtils.isEmpty(initiatedByDto.getUserId()) || StringUtils.isEmpty(initiatedByDto.getAppId())) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }

    CommonUtils.checkUserMandatoryValues(communityMemberWrapperDto.getRequest().getMemberDto());

    if (StringUtils.isEmpty(communityMemberWrapperDto.getRequest().getCommunityId())
        || StringUtils.isEmpty(
            communityMemberWrapperDto.getRequest().getInitiatedByDto().getRealmId())
        || StringUtils.isEmpty(communityMemberWrapperDto.getRequest().getMemberDto().getPhone())
        || communityMemberWrapperDto.getRequest().getMemberDto().getAddress() == null) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }
  }
  public GemsEntityResponseDto createCommunity(CommunityClientWrapperDto communityClientWrapperDto){
    createCommunityMandatoryValuesCheck(communityClientWrapperDto);
    List<Integer> nfcUserIds = new ArrayList<>();
    nfcUserIds.add(communityClientWrapperDto.getRequest().getInitiatedBy().getNfcUserId());
    if(communityClientWrapperDto.getRequest().isPartyCommunity()){
      nfcUserIds.add(communityClientWrapperDto.getRequest().getOtherParty().getNfcUserId());
    }
    Set<String> userIds = new HashSet<>();
    List<String> matrixIds = getMatrixUserIds(nfcUserIds);
    userIds.addAll(matrixIds);
    CommunityDescriptorDto communityDescriptorDto = CommunityDescriptorDto.builder()
            .name(communityClientWrapperDto.getRequest().getGroupName())
            .shortDesc(communityClientWrapperDto.getRequest().getGroupName())
            .longDesc(communityClientWrapperDto.getRequest().getGroupName())
            .media(Media.builder()
                    .type("DisplayPicture")
                    .media(Arrays.asList(MediaDetails.builder()
                                    .mediaUrl(communityClientWrapperDto.getRequest().getImageUrl())
                                    .mediaType("image/jpeg")
                            .build()))
                    .build())
            .build();
    MembersObject member = createMembersObject(communityClientWrapperDto);
    List<MembersObject> members = new ArrayList<>();
    if(communityClientWrapperDto.getRequest().isPartyCommunity()){
      MembersObject otherMember = createOtherMemberObject(communityClientWrapperDto);
      members.add(otherMember);
    }
    members.add(member);
    RoomCreationDto roomCreationDto = getRoomCreationDto(communityClientWrapperDto, userIds);
    RoomCreationResponse roomCreationResponse = roomService.createAndJoinUsers(roomCreationDto);
    if(roomCreationResponse.getRoomId() == null){
      throw CommonUtils.logAndGetException("room not created");
    }
    CommunityEntityDto communityEntityDto = CommunityEntityDto.builder()
            .partyCommunity(communityClientWrapperDto.getRequest().isPartyCommunity())
            .chatRoomId(roomCreationResponse.getRoomId())
            .descriptor(communityDescriptorDto)
            .realmId(communityClientWrapperDto.getRequest().getInitiatedBy().getRealmId())
            .userId(communityClientWrapperDto.getRequest().getInitiatedBy().getUserId())
            .type(communityClientWrapperDto.getRequest().getType())
            .communityType(communityClientWrapperDto.getRequest().getCommunityType())
            .createdAt(CommonUtils.dateFormatterLong().format(new Date()))
            .members(members)
            .createdBy(communityClientWrapperDto.getRequest().getInitiatedBy().getNfcUserId().toString())
            .build();
    if(communityEntityDto.getChatRoomId() == null){
      throw CommonUtils.logAndGetException("chat room id does not exist");
    }
    GemsEntityResponseDto gemsEntityResponseDto = new GemsEntityResponseDto();
    gemsEntityResponseDto.setCommunity(communityEntityDto);
    GemsEntityResponseDto response = gemsRestService.createGemsEntity(CommonConstants.DEFAULT_REALM, String.valueOf(CommonConstants.DEFAULT_USERID), COMMUNITY_ENTITY, gemsEntityResponseDto);
    if(response == null){
      throw CommonUtils.logAndGetException("community not created");
    }
    return response;
  }

  private static RoomCreationDto getRoomCreationDto(CommunityClientWrapperDto communityClientWrapperDto, Set<String> userIds) {
    return RoomCreationDto.builder()
            .name(communityClientWrapperDto.getRequest().getGroupName())
            .userIds(userIds)
            .creationContent(new FederationClass())
            .preset(RoomPresetEnum.PRIVATE_CHAT.getType())
            .topic(DEFAULT_ROOM_TOPIC)
            .visibility(RoomVisibilityEnum.PRIVATE.getType())
            .build();
  }

  private static MembersObject createMembersObject(CommunityClientWrapperDto communityClientWrapperDto) {
    InitiatedByDto initiatedBy = communityClientWrapperDto.getRequest().getInitiatedBy();
    CommunityAddressDto addressDto = CommunityAddressDto.builder()
            .district(initiatedBy.getAddress().getCity())
            .pincode(initiatedBy.getAddress().getAreaCode())
            .locality(initiatedBy.getAddress().getLocality())
            .state(initiatedBy.getAddress().getState())
            .village(initiatedBy.getAddress().getCity())
            .plot(initiatedBy.getAddress().getDoor())
            .landmark(initiatedBy.getAddress().getLandmark())
            .latitude(initiatedBy.getAddress().getLatitude())
            .longitude(initiatedBy.getAddress().getLongitude())
            .build();

    CommunityContactObject contact = CommunityContactObject.builder()
            .contactNumber(Arrays.asList(initiatedBy.getPhone()))
            .build();

    return MembersObject.builder()
            .nfcUserId(initiatedBy.getNfcUserId().toString())
            .providerId(communityClientWrapperDto.getContext().getProviderId())
            .name(initiatedBy.getName())
            .userId(initiatedBy.getUserId())
            .address(addressDto)
            .contact(contact)
            .nfcUserId(initiatedBy.getNfcUserId().toString())
            .admin(true)
            .build();
  }
  private static MembersObject createOtherMemberObject(CommunityClientWrapperDto communityClientWrapperDto) {
    SellerDto otherMember = communityClientWrapperDto.getRequest().getOtherParty();
    CommunityAddressDto addressDto = CommunityAddressDto.builder()
            .district(otherMember.getAddress().getCity())
            .pincode(otherMember.getAddress().getAreaCode())
            .locality(otherMember.getAddress().getLocality())
            .state(otherMember.getAddress().getState())
            .village(otherMember.getAddress().getCity())
            .plot(otherMember.getAddress().getDoor())
            .landmark(otherMember.getAddress().getLandmark())
            .latitude(otherMember.getAddress().getLatitude())
            .longitude(otherMember.getAddress().getLongitude())
            .build();

    CommunityContactObject contact = CommunityContactObject.builder()
            .contactNumber(Arrays.asList(otherMember.getPhone()))
            .build();

    return MembersObject.builder()
            .nfcUserId(otherMember.getNfcUserId().toString())
            .providerId(communityClientWrapperDto.getContext().getProviderId())
            .name(otherMember.getName())
            .userId(otherMember.getUserId())
            .address(addressDto)
            .contact(contact)
            .nfcUserId(otherMember.getNfcUserId().toString())
            .admin(false)
            .build();
  }

  private static void ValueCheckForOtherMemberObject(CommunityClientWrapperDto communityClientWrapperDto) {
    SellerDto otherParty = communityClientWrapperDto.getRequest().getOtherParty();
    if(otherParty.getNfcUserId() == null){
      throw new IllegalArgumentException("nfc user id is required in other party");
    }
    if(StringUtils.isEmpty(otherParty.getName())){
      throw new IllegalArgumentException("name is required in other party");
    }
    if(StringUtils.isEmpty(otherParty.getUserId())){
      throw new IllegalArgumentException("user id is required in other party");
    }
    if(StringUtils.isEmpty(otherParty.getAppId())){
      throw new IllegalArgumentException("app id is required in other party");
    }
    if(communityClientWrapperDto.getRequest().getOtherParty().getAddress() == null){
      throw new IllegalArgumentException("address is required in other party");
    }
  }

  private void createCommunityMandatoryValuesCheck(CommunityClientWrapperDto communityClientWrapperDto) {
    if (communityClientWrapperDto == null
            || communityClientWrapperDto.getContext() == null
            || communityClientWrapperDto.getRequest() == null) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }
    if(communityClientWrapperDto.getRequest().isPartyCommunity()){
      ValueCheckForOtherMemberObject(communityClientWrapperDto);
    }
    if(communityClientWrapperDto.getRequest().isPartyCommunity() && communityClientWrapperDto.getRequest().getOtherParty()== null){
      throw CommonUtils.logAndGetException("Invalid Input");
    }
    InitiatedByDto initiatedByDto = communityClientWrapperDto.getRequest().getInitiatedBy();
    if(initiatedByDto == null || initiatedByDto.getNfcUserId() == null || StringUtils.isEmpty(initiatedByDto.getUserId()) || StringUtils.isEmpty(initiatedByDto.getAppId())) {
      throw CommonUtils.logAndGetException("nfc user id or app id or user id does not exist");
    }
    if (StringUtils.isEmpty(communityClientWrapperDto.getRequest().getType())){
      throw CommonUtils.logAndGetException("invalid request type or request type does not exist");
    }
    if (StringUtils.isEmpty(
            communityClientWrapperDto.getRequest().getInitiatedBy().getRealmId())
            || StringUtils.isEmpty(communityClientWrapperDto.getRequest().getInitiatedBy().getPhone())
            || communityClientWrapperDto.getRequest().getInitiatedBy().getAddress() == null){
      throw CommonUtils.logAndGetException("invalid realm id or phone number");
    }
  }

  public void leaveUser(LeaveCommunityWrapperDto leaveCommunityWrapperDto) {
    checkLeaveUserMandatoryValues(leaveCommunityWrapperDto);
    GemsEntityResponseDto gemsEntityResponseDto =
        gemsRestService.findGemsEntityById(
            leaveCommunityWrapperDto.getRequest().getCommunityId(),
            CommonConstants.DEFAULT_REALM,
            String.valueOf(CommonConstants.DEFAULT_USERID),
            COMMUNITY_ENTITY);

    if (gemsEntityResponseDto == null) {
      throw CommonUtils.logAndGetException("Invalid Community");
    }

    InitiatedByDto initiatedByDto = leaveCommunityWrapperDto.getRequest().getInitiatedByDto();
    boolean userPresent =
        checkIfUserIdIsAlreadyPartOfMembers(
            gemsEntityResponseDto,
            SellerDto.builder().nfcUserId(initiatedByDto.getNfcUserId()).build());

    if (userPresent) {
      gemsEntityResponseDto = removeUser(gemsEntityResponseDto, initiatedByDto.getNfcUserId());
      saveUpdateEntity(
          CommunityMemberDto.builder()
              .communityId(leaveCommunityWrapperDto.getRequest().getCommunityId())
              .build(),
          gemsEntityResponseDto);
    }

    String chatUserId = getMatrixUserId(initiatedByDto.getNfcUserId());
    matrixRestService.kickUserFromRoom(
        leaveCommunityWrapperDto.getRequest().getChatRoomId(),
        KickUserDto.builder().userId(chatUserId).reason(DEFAULT_LEAVE_ROOM_REASON).build());
  }

  private GemsEntityResponseDto removeUser(
      GemsEntityResponseDto gemsEntityResponseDto, Integer nfcUserId) {
    List<MembersObject> updatedMembersObject =
        gemsEntityResponseDto.getCommunity().getMembers().stream()
            .filter(
                e ->
                    !StringUtils.isEmpty(e.getNfcUserId())
                        && !e.getNfcUserId().equals(nfcUserId.toString()))
            .collect(Collectors.toList());
    gemsEntityResponseDto.getCommunity().setMembers(updatedMembersObject);
    return gemsEntityResponseDto;
  }

  private void checkLeaveUserMandatoryValues(LeaveCommunityWrapperDto leaveCommunityWrapperDto) {
    if (leaveCommunityWrapperDto == null
        || leaveCommunityWrapperDto.getContext() == null
        || leaveCommunityWrapperDto.getRequest() == null) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }

    LeaveCommunityGroupDto leaveCommunityGroupDto = leaveCommunityWrapperDto.getRequest();

    if (StringUtils.isEmpty(leaveCommunityGroupDto.getCommunityId())
        || StringUtils.isEmpty(leaveCommunityGroupDto.getChatRoomId())
        || leaveCommunityGroupDto.getInitiatedByDto() == null) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }

    if (leaveCommunityGroupDto.getInitiatedByDto().getNfcUserId() == null) {
      throw CommonUtils.logAndGetException("Invalid Input");
    }
  }
}