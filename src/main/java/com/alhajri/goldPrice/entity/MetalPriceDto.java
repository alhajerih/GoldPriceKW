package com.alhajri.goldPrice.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MetalPriceDto {
    private int metalType;
    private BigDecimal buyPrice24;
    private int priceStatus;
    private int updateIntervalInSeconds;
}
