package com.alhajri.goldPrice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Setter
@Getter
@AllArgsConstructor
public class MetalCfdResult {

    private int metalType;
    private BigDecimal buyPrice24KWD; // KWD per gram
    private long cfdPriceUSD;         // USD CFD per troy ounce
}
