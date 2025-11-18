package com.ninjacart.nfcservice.notifications;

import com.ninjacart.nfcservice.dtos.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private FcmPushNotifier fcmPushNotifier;

    @PostMapping("/push")
    public ApiResponse<?> sendNotificationViaFCM(@RequestBody List<PushNotificationRequest> pushNotificationRequests) throws ExecutionException, InterruptedException {
        fcmPushNotifier.sendMessageToToken(pushNotificationRequests);
        return new ApiResponse<>("Success");
    }
}
