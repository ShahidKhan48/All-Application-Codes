package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.dtos.*;
import com.ninjacart.nfcservice.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/{realm_id}/{user_id}/community")
@RestController
public class CommunityController {
  @Autowired private CommunityService communityService;

  @PostMapping("/member")
  public GemsEntityResponseDto addMemberToCommunity(
      @RequestBody CommunityMemberWrapperDto communityMemberWrapperDto) throws Exception {
    return communityService.addMember(communityMemberWrapperDto);
  }

  @PostMapping
  public GemsEntityResponseDto createCommunity(
      @RequestBody CommunityClientWrapperDto communityClientWrapperDto) throws Exception {

    return communityService.createCommunity(communityClientWrapperDto);
  }

  @PostMapping("/leave")
  public LeaveCommunityResponse leaveCommunity(
      @RequestBody LeaveCommunityWrapperDto leaveCommunityWrapperDto) throws Exception {

    communityService.leaveUser(leaveCommunityWrapperDto);
    return LeaveCommunityResponse.builder().status("Success").build();
  }
}
