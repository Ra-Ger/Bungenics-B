package com.kodilla.bungenics.dataFetchers.OpenMeteo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

@Component
public class WeatherFetcher {

    private static final String WEATHER_BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String GEOCODING_BASE_URL = "https://nominatim.openstreetmap.org/search";
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public WeatherFetcher(OkHttpClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public WeatherRecord fetchCurrentWeather(double latitude, double longitude) throws Exception {
        String url = WEATHER_BASE_URL
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current_weather=true"
                + "&timezone=auto"
                + "&format=json";

        return executeWeatherRequest(url, latitude, longitude);
    }

    public WeatherRecord fetchCurrentWeatherForCity(String city) throws Exception {
        double[] coords = getCoordinatesFromCityName(city);
        return fetchCurrentWeather(coords[0], coords[1]);
    }

    private double[] getCoordinatesFromCityName(String city) throws Exception {
        String url = GEOCODING_BASE_URL
                + "?q=" + city.replace(" ", "+")
                + "&format=json"
                + "&limit=1";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RabbitFeedingApp/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Geocoding error: " + response.code());
            }

            String jsonResponse = response.body().string();
            JsonNode rootNode = mapper.readTree(jsonResponse);

            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode first = rootNode.get(0);
                double lat = first.path("lat").asDouble();
                double lon = first.path("lon").asDouble();
                return new double[]{lat, lon};
            }
            throw new RuntimeException("City not found: " + city);
        }
    }

    private WeatherRecord executeWeatherRequest(String url, double latitude, double longitude) throws Exception {
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Bad weather request: " + response.code());
            }

            String jsonResponse = response.body().string();
            JsonNode rootNode = mapper.readTree(jsonResponse);

            JsonNode currentWeather = rootNode.path("current_weather");
            if (currentWeather.isMissingNode()) {
                throw new RuntimeException("No current weather data in response");
            }

            double temperature = currentWeather.path("temperature").asDouble();
            double windSpeed = currentWeather.path("windspeed").asDouble();
            int weatherCode = currentWeather.path("weathercode").asInt();
            String timestamp = currentWeather.path("time").asText();

            double humidity = fetchHumidity(latitude, longitude);
            String weatherDescription = WeatherCodeMapper.getDescription(weatherCode);
            String location = String.format("%.2f,%.2f", latitude, longitude);

            return new WeatherRecord(
                    temperature,
                    humidity,
                    windSpeed,
                    weatherCode,
                    weatherDescription,
                    timestamp,
                    location
            );
        }
    }

    private double fetchHumidity(double latitude, double longitude) throws Exception {
        String humidityUrl = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&hourly=relative_humidity_2m"
                + "&timezone=auto"
                + "&forecast_days=1"
                + "&format=json";

        Request request = new Request.Builder().url(humidityUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return 0.0;
            }

            String jsonResponse = response.body().string();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode hourly = rootNode.path("hourly");
            JsonNode humidities = hourly.path("relative_humidity_2m");

            if (humidities.isArray() && humidities.size() > 0) {
                return humidities.path(0).asDouble(0.0);
            }
            return 0.0;
        }
    }
}
