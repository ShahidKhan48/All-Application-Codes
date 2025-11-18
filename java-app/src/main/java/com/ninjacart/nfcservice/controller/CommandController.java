package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.dtos.CommandCreationResponseDto;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandProcessorResponseDto;
import com.ninjacart.nfcservice.dtos.CommandRequestWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateWrapperDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/{realm_id}/{user_id}/command")
public class CommandController {

  @Autowired private CommandService commandService;


  @PostMapping("/status")
  public CommandProcessorResponseDto updateCommandStatus(
      @RequestBody @Valid CommandRequestWrapperDto commandRequestWrapperDto) throws Exception {
    return commandService.process(commandRequestWrapperDto);
  }

  @PostMapping("/{command_type}")
  public CommandCreationResponseDto createCommand(
      @PathVariable(name = "command_type") String commandType,
      @RequestBody CommandCreationWrapperDto commandCreationWrapperDto)
      throws Exception {
    return commandService.create(commandCreationWrapperDto, commandType);
  }

  @DeleteMapping("/{command_type}")
  public CommandCreationResponseDto deleteCommand(
          @PathVariable(name = "command_type") String commandType,
          @RequestBody CommandCreationWrapperDto commandCreationWrapperDto)
          throws Exception {
    return commandService.delete(commandCreationWrapperDto, commandType);
  }

  @PostMapping("/status/{command_type}")
  public CommandProcessorResponseDto updateCommand(
          @PathVariable(name = "command_type") CommandTypeEnum commandType,
          @RequestBody  CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) throws Exception {
    return commandService.processV1(commandStatusUpdateWrapperDto, commandType);
  }
}
