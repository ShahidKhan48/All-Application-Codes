package com.ninjacart.nfcservice.converters;

import com.ninjacart.nfcservice.dtos.OrderManagementItemsDto;
import com.ninjacart.nfcservice.dtos.OrderManagementRequestObjectDto;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import com.ninjacart.nfcservice.dtos.request.Message;
import com.ninjacart.nfcservice.dtos.request.PriceDto;
import com.ninjacart.nfcservice.dtos.request.QuantityDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.dtos.request.RequestObjectDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderManagementToRequestDtoConverter {

  private static final String DEFAULT_CURRENCY = "INR";

  public ItemsDto omsItemsToRequestItemsConverter(OrderManagementItemsDto orderManagementItemsDto) {
    return ItemsDto.builder()
        .id(
            orderManagementItemsDto.getId() != null
                ? orderManagementItemsDto.getId().toString()
                : "")
        .itemType(
            orderManagementItemsDto.getItemTypeId() != null
                ? orderManagementItemsDto.getItemTypeId().toString()
                : "")
        .name(orderManagementItemsDto.getItemName())
        .imageURL(orderManagementItemsDto.getImageURL())
        .uomName(orderManagementItemsDto.getItemUomName())
        .packing(orderManagementItemsDto.getPacking())
        .price(
            PriceDto.builder()
                .currency(DEFAULT_CURRENCY)
                .value(orderManagementItemsDto.getUnitBuyPrice())
                .build())
        .quantity(
            QuantityDto.builder()
                .count(
                    orderManagementItemsDto.getQuantity() != null
                        ? orderManagementItemsDto.getQuantity()
                        : null)
                .build())
        .build();
  }

  public List<ItemsDto> omsItemsToRequestItemsConverter(
      List<OrderManagementItemsDto> orderManagementItemsDtos) {
    if (CollectionUtils.isEmpty(orderManagementItemsDtos)) {
      return new ArrayList<>();
    }

    List<ItemsDto> itemsDtos = new ArrayList<>();

    for (OrderManagementItemsDto each : orderManagementItemsDtos) {
      itemsDtos.add(omsItemsToRequestItemsConverter(each));
    }

    return itemsDtos;
  }

  public RequestObjectDto convertOmsResponseToFFERequest(
      OrderManagementRequestObjectDto orderManagementRequestObjectDto) {
    return RequestObjectDto.builder()
        .approvals(orderManagementRequestObjectDto.getApprovals())
        .buyer(orderManagementRequestObjectDto.getBuyer())
        .requestType(orderManagementRequestObjectDto.getRequestType())
        .entityStatus(orderManagementRequestObjectDto.getEntityStatus())
        .entityVersion(orderManagementRequestObjectDto.getEntityVersion())
        .externalReferenceId(orderManagementRequestObjectDto.getExternalReferenceId())
        .chatRoomId(orderManagementRequestObjectDto.getChatRoomId())
        .id(orderManagementRequestObjectDto.getId())
        .items(omsItemsToRequestItemsConverter(orderManagementRequestObjectDto.getItems()))
        .message(orderManagementRequestObjectDto.getMessage())
        .requestGroupId(orderManagementRequestObjectDto.getRequestGroupId())
        .userRoleDtos(orderManagementRequestObjectDto.getUserRoleDtoList())
        .ownerId(orderManagementRequestObjectDto.getOwnerId())
        .orderId(orderManagementRequestObjectDto.getOrderId())
        .seller(orderManagementRequestObjectDto.getSeller())
        .status(orderManagementRequestObjectDto.getStatus())
        .active(orderManagementRequestObjectDto.getActive())
        .shippingAddress(orderManagementRequestObjectDto.getShippingAddress())
        .deliveryDate(orderManagementRequestObjectDto.getDeliveryDate())
        .paymentTerms(orderManagementRequestObjectDto.getPaymentTerms())
        .qualityTerms(orderManagementRequestObjectDto.getQualityTerms())
        .freeFlowPartyDtos(orderManagementRequestObjectDto.getFreeFlowPartyDtos())
        .build();
  }
}
