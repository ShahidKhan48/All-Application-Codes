package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.dtos.ProviderDto;
import com.ninjacart.nfcservice.dtos.SearchRequestDto;
import com.ninjacart.nfcservice.dtos.SearchResponseDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.classfile.Module.Provide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SearchService {

  @Autowired
  private WorkflowRestService workflowRestService;

  @Autowired
  private TenantRestService tenantRestService;

  public SearchResponseDto searchFanOut(SearchRequestDto searchRequestDto) {

    SearchResponseDto broadCastSearchResponse = workflowRestService.forwardSearchRequest(
        searchRequestDto);

    List<SellerDto> sellerDtos = broadCastSearchResponse.getMessage();

    List<SellerDto> responseSellerDtos = new ArrayList<>();

    for (SellerDto sellerDto : sellerDtos) {
      ProviderDto providerDto = ProviderDto.builder()
          .id("25c3ed73-cd4b-4dac-8a7c-473a6e359f38")
          .name("Bayer")
          .build();
      sellerDto.setProvider(providerDto);
      responseSellerDtos.add(sellerDto);
    }

    SearchResponseDto searchResponseDto = SearchResponseDto.builder()
        .context(searchRequestDto.getContext())
        .message(responseSellerDtos)
        .build();

    return searchResponseDto;
  }
}
