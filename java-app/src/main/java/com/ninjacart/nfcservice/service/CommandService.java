package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.dtos.CommandCreationResponseDto;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandProcessorResponseDto;
import com.ninjacart.nfcservice.dtos.CommandRequestDto;
import com.ninjacart.nfcservice.dtos.CommandRequestWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateWrapperDto;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommandService {

  @Autowired private List<AbstractCommandProcessorService> commandProcessors;

  @Autowired private List<AbstractCommandCreationService> commandCreationProcessors;

  private static Map<String, AbstractCommandProcessorService> commandProcessorPool =
      new HashMap<>();

  @Autowired private  List<AbstractCommandDeletionService> commandDeletion;

  private static  Map<String, AbstractCommandDeletionService> commandDeletionPool = new HashMap<>();

  private static Map<String, AbstractCommandCreationService> commandCreationProcessorPool =
      new HashMap<>();

  @PostConstruct
  public void initPool() {
    commandProcessors.forEach(
        each -> {
          commandProcessorPool.put(each.getCommandType(), each);
        });

    commandCreationProcessors.forEach(
        each -> {
          commandCreationProcessorPool.put(each.getCommandType(), each);
        });
    commandDeletion.forEach(
            each -> {
              commandDeletionPool.put(each.getCommandType(), each);

            }
    );
  }

  public CommandProcessorResponseDto process(CommandRequestWrapperDto commandRequestWrapperDto)
      throws Exception {

    AbstractCommandProcessorService commandService =
        commandProcessorPool.get(commandRequestWrapperDto.getRequest().getCommandType().name());

    if (commandService == null) {
      throw new RuntimeException("Invalid commandType");
    }

    return commandService.process(commandRequestWrapperDto);
  }

  public CommandCreationResponseDto create(
      CommandCreationWrapperDto commandCreationWrapperDto, String commandType) throws Exception {

    AbstractCommandCreationService commandService = commandCreationProcessorPool.get(commandType);

    if (commandService == null) {
      throw new RuntimeException("Invalid commandType");
    }

    return commandService.create(commandCreationWrapperDto);
  }
  //todo need to deprecate process fn and to have generic dto
  public CommandProcessorResponseDto processV1(CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto, CommandTypeEnum commandType)
          throws Exception {

    AbstractCommandProcessorService commandService =
            commandProcessorPool.get(commandType.name());

    if (commandService == null) {
      throw new RuntimeException("Invalid commandType");
    }

    return commandService.processV1(commandStatusUpdateWrapperDto);
  }
  public CommandCreationResponseDto delete(
          CommandCreationWrapperDto commandCreationWrapperDto, String commandType) throws Exception {

    AbstractCommandDeletionService commandService = commandDeletionPool.get(commandType);

    if (commandService == null) {
      throw new RuntimeException("Invalid commandType");
    }

    return commandService.delete(commandCreationWrapperDto);
  }


}
