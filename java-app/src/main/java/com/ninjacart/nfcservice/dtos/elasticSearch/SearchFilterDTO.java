package com.ninjacart.nfcservice.dtos.elasticSearch;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class SearchFilterDTO {
    private int size;
    private Query query;


//    private Map<String,Object> query;

}
