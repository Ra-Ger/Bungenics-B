package com.kodilla.bungenics.dataFetchers.NASS;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommodityPrice {
    private final float price;
    private final String description;
}
