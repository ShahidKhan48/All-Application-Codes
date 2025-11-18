package com.ninjacart.nfcservice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.dtos.CommandCreationResponseDto;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.dtos.CommunityChatRoomDto;
import com.ninjacart.nfcservice.dtos.ForwardCatalogDTO;
import com.ninjacart.nfcservice.dtos.catalog.CommunityCatalog;
import com.ninjacart.nfcservice.dtos.catalog.CommunityCatalogGemsDTO;
import com.ninjacart.nfcservice.dtos.catalog.ShareCatalogCommandDto;
import com.ninjacart.nfcservice.dtos.catalog.ShareCatalogRequestDto;
import com.ninjacart.nfcservice.enums.CommandTypeEnum;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Builder
@Slf4j
public class ForwardCatalogService extends AbstractCommandCreationService {
    @Autowired
    private CatalogService catalogService;

    private static final String FUNCTION_RESPONSE = "Shared Successfully";

    @Override
    public String getCommandType() {
        return CommandTypeEnum.FORWARD_CATALOG.name();
    }

    @Override
    public CommandCreationResponseDto create(CommandCreationWrapperDto commandCreationWrapperDto) throws Exception {
        if (commandCreationWrapperDto == null) {
            throw CommonUtils.logAndGetException("Bad Request");
        }
        ForwardCatalogDTO forwardCatalogDTO = parseAndGetBody(commandCreationWrapperDto);
        List<CommunityChatRoomDto> roomIdList = forwardCatalogDTO.getChatRoomDto();
        if (CollectionUtils.isEmpty(roomIdList)) {
            throw CommonUtils.logAndGetException("Bad Request");
        }
        for (CommunityChatRoomDto dto : forwardCatalogDTO.getChatRoomDto()) {
            CommunityCatalog communityCatalog=null;
            communityCatalog = forwardCatalogDTO.getCommunityCatalog();
            communityCatalog.setCommunityId(dto.getCommunityId());

            catalogService.shareCatalogwithMatrix(ShareCatalogCommandDto.builder().communityCatalog(communityCatalog).initiatedByDto(commandCreationWrapperDto.getMessage().getInitiatedByDto()).build()
            ,ShareCatalogRequestDto.builder().chatRoomId(dto.getChatRoomId()).build()
            );
            catalogService.addCatalogToGems(ShareCatalogRequestDto.builder().chatRoomId(dto.getChatRoomId()).communityCatalog(communityCatalog).build());

        }
        return CommandCreationResponseDto.builder().status(FUNCTION_RESPONSE).build();

    }

    private ForwardCatalogDTO parseAndGetBody(CommandCreationWrapperDto commandCreationWrapperDto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(commandCreationWrapperDto.getMessage().getRequest(), ForwardCatalogDTO.class);
        } catch (Exception e) {
            throw CommonUtils.logAndGetException("Invalid Input", e);
        }
    }
}
