package com.alhajri.goldPrice.DTO;

import com.alhajri.goldPrice.entity.MetalCfdResult;
import com.alhajri.goldPrice.entity.MetalPriceDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MetalPriceDao {

    CompletableFuture<List<MetalPriceDto>> getMetalPricesAsync();
    CompletableFuture<List<MetalCfdResult>>getMetalPricesWithCfdAsync();
}

