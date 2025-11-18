package com.ninjacart.nfcservice.notifications;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class FcmPushNotifier {

    public void sendMessageToToken(List<PushNotificationRequest> requests){

        for(PushNotificationRequest request: requests) {
            try{
                Message message = generateMessageForToken(request);
                String response = sendAndGetResponse(message);
                log.debug("Sent message : {} to token. Device token: {} ---> response : {}", request.toString(), request.getToken(), response);
            } catch (Exception ex){
                log.error("Error while notifying " + ex.getMessage());
            }
        }
    }

    private Message generateMessageForToken(PushNotificationRequest request) {

        Notification notification = new Notification(request.getTitle(), request.getMessage());

        return Message.builder()
                .setNotification(notification)
                .putData("notificationData", request.getData())
                .putData("click_action", request.getUrl())
                .setToken(request.getToken())
                .build();
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }
}
