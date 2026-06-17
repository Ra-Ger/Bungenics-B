package com.kodilla.bungenics.dataFetchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodilla.bungenics.dataFetchers.NASS.CommodityCode;
import com.kodilla.bungenics.dataFetchers.NASS.CommodityPrice;
import com.kodilla.bungenics.dataFetchers.NASS.NASSLatestDataFetcher;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NASSLatestDataFetcherTest {

    @Mock
    private OkHttpClient mockClient;

    @Mock
    private Call mockCall;

    @Mock
    private Response mockResponse;

    @Mock
    private ResponseBody mockResponseBody;

    private NASSLatestDataFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new NASSLatestDataFetcher(mockClient, new ObjectMapper());
    }

    @Test
    void shouldHandleNoDataScenario() throws Exception {
        // Given
        String emptyJson = "[]";

        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn(emptyJson);

        // When
        CommodityPrice result = fetcher.fetchLatestPriceForCommodity(CommodityCode.LETTUCE);

        // Then
        assertNotNull(result);
        assertEquals(-1, result.getPrice(), "Price should be -1 for no data");
    }
}