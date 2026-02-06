package com.alhajri.goldPrice.DTO;

import com.alhajri.goldPrice.entity.MetalPriceDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetalPricesResponse {

    @JsonProperty("isSuccess")
    private boolean isSuccess;
    private String message;
    private List<MetalPriceDto> result;
}

