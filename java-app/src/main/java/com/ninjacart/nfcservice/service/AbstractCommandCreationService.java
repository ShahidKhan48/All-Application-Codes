package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.dtos.CommandCreationResponseDto;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.dtos.FetchRequestResponseDto;
import com.ninjacart.nfcservice.dtos.OrderManagementFetchFilter;
import com.ninjacart.nfcservice.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public abstract class AbstractCommandCreationService {

    @Autowired private OrderManagementRestService orderManagementRestService;

    @Autowired private TenantRestService tenantRestService;

    private static final String NFC_REQUEST_TYPE = "NFC_REQUEST";
    //TODO CHANGE
    private static final String OUTPUT_TEMPLATE = "nfc-fetch-request-filter_2";

    public abstract String getCommandType();
    @LogExecutionTime
    public abstract CommandCreationResponseDto create(CommandCreationWrapperDto commandCreationWrapperDto) throws Exception;

    protected FetchRequestResponseDto fetchRequestByExternalReference(String externalReferenceId) {
        return orderManagementRestService.fetchRequest(OrderManagementFetchFilter.builder()
                .externalReferenceId(externalReferenceId)
                .entityType(NFC_REQUEST_TYPE)
                .build(), OUTPUT_TEMPLATE);
    }

    protected void forwardRequestToTenant(Object requestBody, String url) {
        tenantRestService.triggerApiToTenant(requestBody, url);
    }

    protected FetchRequestResponseDto getOrderByExternalReferenceIdAndVersion(String externalReferenceId,
                                                                              Integer entityVersion, Boolean active) {
        System.out.println(entityVersion);
        FetchRequestResponseDto fetchRequestResponseDto =
                fetchRequestByExternalReferenceAndVersion(externalReferenceId, entityVersion, active);

        if (fetchRequestResponseDto == null || CollectionUtils.isEmpty(
                fetchRequestResponseDto.getRequests())) {
            throw CommonUtils.logAndGetException("Refresh the screen and try again");
        }
        return fetchRequestResponseDto;
    }

    protected FetchRequestResponseDto fetchRequestByExternalReferenceAndVersion(
            String externalReferenceId, Integer entityVersion, Boolean active) {
        return orderManagementRestService.fetchRequest(OrderManagementFetchFilter.builder()
                .externalReferenceId(externalReferenceId)
                .entityType(NFC_REQUEST_TYPE)
                .active(active)
                .entityVersion(entityVersion)
                .build(), OUTPUT_TEMPLATE);
    }

}
