package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.dtos.*;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.service.AdminService;
import com.ninjacart.nfcservice.service.ProvideruserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/{realm_id}/{user_id}/admin")
@RestController
public class AdminController {

  @Autowired private ProvideruserService provideruserService;

  @Autowired private AdminService adminService;

  @GetMapping
  public ApiResponse<AdminResponseDto> getAdminDetails(
      @RequestParam("provider_id") String providerId,
      @RequestParam("provider_user_id") String providerUserId) {
    return new ApiResponse<>(provideruserService.fetchAdminDetails(providerId, providerUserId));
  }
  @PostMapping("/order")
  public RequestDto requestNegotiation1(@RequestBody @Valid RequestDto requestDto) {
    return provideruserService.createOrder(requestDto);
  }
  @PostMapping("/member")
  public String addMemberToChatRoom(@RequestBody @Valid AddMemberDto addMemberDto) {
    adminService.addMember(addMemberDto);
    return "success";
  }
}
