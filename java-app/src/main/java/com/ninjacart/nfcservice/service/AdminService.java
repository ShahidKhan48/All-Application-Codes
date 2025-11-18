package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.dtos.AddMemberDto;
import com.ninjacart.nfcservice.dtos.AddMemberRestCallDto;
import com.ninjacart.nfcservice.dtos.JoinUserDto;
import com.ninjacart.nfcservice.entity.ProviderUser;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminService {

    @Autowired private OrderManagementRestService orderManagementRestService;
    @Autowired private ProvideruserService provideruserService;
    @Autowired private RoomService roomService;
    public void addMember(AddMemberDto addMemberDto) {
        joinMemberToRoom(addMemberDto.getMessage().getRoomId(),addMemberDto.getMessage().getNfcUserId());
        AddMemberRestCallDto addMemberRestCallDto = AddMemberRestCallDto.builder().name(addMemberDto.getMessage().getName())
                .contactNumber(addMemberDto.getMessage().getContactNumber())
                .email(addMemberDto.getMessage().getEmail())
                .partyType(addMemberDto.getMessage().getPartyType())
                .userId(addMemberDto.getMessage().getNfcUserId())
                .externalReferenceId(addMemberDto.getMessage().getExternalReferenceId())
                .build();
         orderManagementRestService.saveMembersOfTheRoom(addMemberRestCallDto);


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
    public void joinMemberToRoom(String roomId, Integer nfcUserId) {
        String chatUserId = getMatrixUserId(nfcUserId);
        roomService.joinAnUserToRoom(JoinUserDto
                .builder()
                .userId(chatUserId)
                .build(), roomId);
    }
}
