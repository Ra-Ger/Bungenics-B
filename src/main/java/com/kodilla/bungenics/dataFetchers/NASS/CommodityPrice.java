package com.kodilla.bungenics.dataFetchers.NASS;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class CommodityPrice {
    private final BigDecimal price;
    private final String description;
}
