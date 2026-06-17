package com.kodilla.bungenics.dataFetchers.NASS;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@PropertySource("classpath:application-vulnerableData.properties")
public class NASSLatestDataFetcher {

    @Value("${nass.api.key}")
    private String apiKey;

    private static final String BASE_URL = "https://quickstats.nass.usda.gov/api/api_GET/";
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public NASSLatestDataFetcher(OkHttpClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    private static final Map<String, Integer> MONTH_MAP = new HashMap<>();
    static {
        String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        for (int i = 0; i < months.length; i++) {
            MONTH_MAP.put(months[i], i + 1);
        }
    }

    public CommodityPrice fetchLatestPriceForCommodity(CommodityCode commodity) throws Exception {
        String url = BASE_URL + "?key=" + apiKey
                + "&commodity_desc=" + commodity
                + "&statisticcat_desc=PRICE%20RECEIVED"
                + "&agg_level_desc=NATIONAL"
                + "&freq_desc=MONTHLY"
                + "&format=JSON";

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Bad request: " + response.code());
            }

            String jsonResponse = response.body().string();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode dataArray = rootNode.get("data");

            if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
                String desc = "No data for " + commodity;
                return new CommodityPrice(-1,desc);
            }

            JsonNode latestRecord = null;
            int latestYear = -1;
            int latestMonth = -1;

            for (JsonNode record : dataArray) {
                JsonNode yearNode = record.get("year");
                if (yearNode == null) continue;
                int year = yearNode.asInt();

                JsonNode periodNode = record.get("reference_period_desc");
                if (periodNode == null) continue;
                String period = periodNode.asText().trim().toUpperCase();
                Integer month = MONTH_MAP.get(period);
                if (month == null) continue;

                if (year > latestYear || (year == latestYear && month > latestMonth)) {
                    latestYear = year;
                    latestMonth = month;
                    latestRecord = record;
                }
            }

            if (latestRecord == null) {
                String desc = "No monthly data for " + commodity;
                return new CommodityPrice(-1,desc);
            }

            JsonNode valueNode = latestRecord.get("Value");
            String period = latestRecord.get("reference_period_desc").asText();
            float price = Float.parseFloat(valueNode.asText());
            String desc = "Latest record for " + commodity + ": " + period + " " + latestYear;

            return new CommodityPrice(price, desc);
        }
    }
}