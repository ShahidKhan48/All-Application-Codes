package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.service.crfilter.CrFilterService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @Autowired
  private CrFilterService crFilterService;
  @GetMapping("/testsearch")
  public Object testSearch(){
//    return crFilterService.getAllCrs(null, Arrays.asList("25c3ed73-cd4b-4dac-8a7c-473a6e359f38"),true);
    return null;
  }
}
