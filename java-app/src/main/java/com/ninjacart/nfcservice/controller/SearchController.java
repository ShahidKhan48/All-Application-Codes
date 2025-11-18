package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.dtos.OnboardDto;
import com.ninjacart.nfcservice.dtos.OnboardResponseDto;
import com.ninjacart.nfcservice.dtos.SearchRequestDto;
import com.ninjacart.nfcservice.dtos.SearchResponseDto;
import com.ninjacart.nfcservice.service.SearchService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/search")
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping
    public SearchResponseDto search( @RequestBody SearchRequestDto searchRequestDto) throws Exception {
        return searchService.searchFanOut(searchRequestDto);
    }
}
