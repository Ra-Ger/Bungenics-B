package com.kodilla.bungenics.dataFetchers;

import com.kodilla.bungenics.dataFetchers.NASS.CommodityCode;
import com.kodilla.bungenics.dataFetchers.NASS.CommodityPrice;
import com.kodilla.bungenics.dataFetchers.NASS.NASSLatestDataFetcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RabbitFeedingApplicationTests {

    @Autowired
    NASSLatestDataFetcher fetcher;

    @Test
    void shouldFetchDataForHay() throws Exception {
        // When
        String result = fetcher.fetchLatestPriceForCommodity(CommodityCode.HAY).getDescription();

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("Latest record") || result.equals("No data"),
                "Expected data or 'No data', fetched: " + result);

        if (result.startsWith("Latest record")) {
            assertTrue(result.matches(".*\\d{4}.*"), "Should contain year");
        }

        System.out.println("result from API: " + result);
    }

    @Test
    void shouldFetchPriceForHay() throws Exception {
        // When
        Float result = fetcher.fetchLatestPriceForCommodity(CommodityCode.HAY).getPrice();

        // Then
        assertNotNull(result);
        assertTrue(result > 0);

        System.out.println("Hay price: " + result);
    }

    @Test
    void shouldFetchDataForSpinach() throws Exception {
        CommodityPrice result = fetcher.fetchLatestPriceForCommodity(CommodityCode.SPINACH);
        assertNotNull(result);
        assertTrue(result.getDescription().contains("SPINACH"));
        System.out.println("spin price: " + result.getPrice());
    }

    @Test
    void shouldFetchDataForLettuce() throws Exception {
        CommodityPrice result = fetcher.fetchLatestPriceForCommodity(CommodityCode.LETTUCE);
        assertNotNull(result);
        assertTrue(result.getDescription().contains("LETTUCE"));
        System.out.println("let price: " + result.getPrice());
    }

    @Test
    void shouldFetchDataForCarrots() throws Exception {
        CommodityPrice result = fetcher.fetchLatestPriceForCommodity(CommodityCode.CARROTS);
        assertNotNull(result);
        assertTrue(result.getDescription().contains("CARROTS"));
        System.out.println("car price: " + result.getPrice());
    }
}
