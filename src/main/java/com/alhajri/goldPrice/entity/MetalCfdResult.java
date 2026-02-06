package com.alhajri.goldPrice.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
public class MetalCfdResult {

    private int metalType;
    private BigDecimal buyPrice24KWD; // KWD per gram
    private long cfdPriceUSD;         // USD CFD per troy ounce

    public MetalCfdResult(int metalType, BigDecimal buyPrice24KWD, long cfdPriceUSD) {
        this.metalType = metalType;
        this.buyPrice24KWD = buyPrice24KWD.setScale(3, BigDecimal.ROUND_UP);
        this.cfdPriceUSD = cfdPriceUSD;
    }
}
