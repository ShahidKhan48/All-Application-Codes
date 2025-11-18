package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatrixEventResponseDto {
    private FetchMessageDto content;
    @JsonProperty("event_id")
    private String eventId;
    @JsonProperty("origin_server_ts")
    private long originServerTs;
    @JsonProperty("room_id")
    private String roomId;
    private String sender;
    private String type;

}
