package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.dtos.FcmInfoDto;
import com.ninjacart.nfcservice.dtos.MatrixUserAuthResponseDto;
import com.ninjacart.nfcservice.dtos.OnboardRequestDto;
import com.ninjacart.nfcservice.dtos.TenantUserOnboardResponseDto;
import com.ninjacart.nfcservice.entity.ProviderUser;
import com.ninjacart.nfcservice.service.ProvideruserService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/{realm_id}/{user_id}/users")
@RestController
public class UserController {

    @Autowired
    private ProvideruserService provideruserService;


    @PostMapping
    public TenantUserOnboardResponseDto createUser(@RequestBody @Valid OnboardRequestDto onboardRequestDto) throws Exception {
        return provideruserService.onboardTenant(onboardRequestDto);
    }

    @GetMapping("matrix-credentials")
    public MatrixUserAuthResponseDto getMatrixAuth(@Valid @RequestParam("nfc_user_id") Integer nfcUserId) {
        return provideruserService.getMatrixAuthDetails(nfcUserId);
    }

    @GetMapping("/get-fcm-id")
    public String getFcmId(@Valid @RequestParam("provider_id") String providerId,@Valid @RequestParam("provider_user_id") String providerUserId) {
        return provideruserService.getFcmId(providerId,providerUserId);
    }

    @PutMapping("/update-fcm-id")
    public ProviderUser updateFcmId(@RequestBody @Valid FcmInfoDto fcmInfoDto) throws Exception {
        return provideruserService.updateFcmId(fcmInfoDto);
    }

}
