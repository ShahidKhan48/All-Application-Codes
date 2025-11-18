package com.ninjacart.nfcservice.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushNotificationRequest {
    private String title;
    private String message;
    private String data;
    private String token;
    private String url;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
