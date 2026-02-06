package com.alhajri.goldPrice.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FxRateResponse {
    public boolean success;
    public Info info;
    public double result;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Info {
        public double rate;
    }
}

