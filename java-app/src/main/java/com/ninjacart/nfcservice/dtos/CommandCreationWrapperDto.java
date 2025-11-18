package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.Context;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandCreationWrapperDto {
  @NotNull @Valid private Context context;

  @NotNull @Valid private CommandCreationDto message;

  @JsonProperty("community_chat_room")
  List<CommunityChatRoomDto> chatRoomDtoList;
}
