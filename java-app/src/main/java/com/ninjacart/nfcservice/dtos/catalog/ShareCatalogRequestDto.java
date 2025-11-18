package com.ninjacart.nfcservice.dtos.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.CommunityChatRoomDto;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ShareCatalogRequestDto {
    @JsonProperty("chat_room_id")
    private String chatRoomId;
    @JsonProperty("catalog")
    private CommunityCatalog communityCatalog;
    @JsonProperty("expiry_days")
    private int expiryDays;

    @JsonProperty("expiry_date")
    private Date expiryAt;

}
