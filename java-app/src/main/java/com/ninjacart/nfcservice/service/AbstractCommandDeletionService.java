package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.dtos.CommandCreationResponseDto;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;

public abstract class AbstractCommandDeletionService {

    public abstract CommandCreationResponseDto delete(CommandCreationWrapperDto commandCreationWrapperDto) throws Exception;

    public abstract String getCommandType();

}
