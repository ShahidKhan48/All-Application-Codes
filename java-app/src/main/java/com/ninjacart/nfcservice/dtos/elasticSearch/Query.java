package com.ninjacart.nfcservice.dtos.elasticSearch;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
@Builder
public class Query {
    private Bool bool;


}
