package com.ninjacart.nfcservice.dtos.catalog;

import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareCatalogCommandDto {
    private InitiatedByDto initiatedByDto;
    private CommunityCatalog communityCatalog;
}
