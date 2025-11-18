package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.dtos.CreateRoomDto;
import com.ninjacart.nfcservice.dtos.JoinUserDto;
import com.ninjacart.nfcservice.dtos.RoomCreationDto;
import com.ninjacart.nfcservice.dtos.RoomCreationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoomService {

  @Autowired private MatrixRestService matrixRestService;

  public RoomCreationResponse createAndJoinUsers(RoomCreationDto roomCreationDto) {

    RoomCreationResponse response =
        matrixRestService.createRoom(
            CreateRoomDto.builder()
                .invite(roomCreationDto.getInvite())
                .creationContent(roomCreationDto.getCreationContent())
                .name(roomCreationDto.getName())
                .preset(roomCreationDto.getPreset())
                .visibility(roomCreationDto.getVisibility())
                .topic(roomCreationDto.getTopic())
                .build());
    for (String each : roomCreationDto.getUserIds()) {
      joinAnUserToRoom(JoinUserDto.builder().userId(each).build(), response.getRoomId());
    }

    return response;
  }

  public RoomCreationResponse joinAnUserToRoom(JoinUserDto joinUserDto, String roomId) {
    return matrixRestService.joinUserToRoom(roomId, joinUserDto);
  }
}
