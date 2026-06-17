package com.kodilla.bungenics.dataFetchers;

import com.kodilla.bungenics.dataFetchers.OpenMeteo.SuggestedLocation;
import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherFetcher;
import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
 class OpenMeteoTest {

    @Autowired
    WeatherFetcher weatherFetcher;

    @Test
     void shouldFetchWeatherForCoordinates() {
        assertDoesNotThrow(() -> {
            WeatherRecord w1 = weatherFetcher.fetchCurrentWeather(52.2297, 21.0122);
            System.out.println(w1);
        });
    }

    @Test
     void shouldFetchWeatherForCity() {
        assertDoesNotThrow(() -> {
            WeatherRecord w1 = weatherFetcher.fetchCurrentWeatherForCity("Krakow");
            System.out.println(w1);
        });
    }

    @Test
     void shouldThrow() {
        assertThrows(RuntimeException.class, () -> {
            WeatherRecord w1 = weatherFetcher.fetchCurrentWeatherForCity("XD123");
        });
    }

    @Test
    void shouldFetchSugestedLocations() {
        //Given
        boolean allFetched = true;
        WeatherRecord wr;

        //when
        try {
            wr = weatherFetcher.fetchCurrentWeatherForCity("Stokkseyri");
            System.out.println(wr);

            wr = weatherFetcher.fetchCurrentWeatherForCity("Hambleden");
            System.out.println(wr);

            wr = weatherFetcher.fetchCurrentWeatherForCity("Pęcice");
            System.out.println(wr);

            wr = weatherFetcher.fetchCurrentWeatherForCity("Prokshino");
            System.out.println(wr);

            wr = weatherFetcher.fetchCurrentWeatherForCity("Saqqara");
            System.out.println(wr);

            wr = weatherFetcher.fetchCurrentWeatherForCity("Cuandixia");
            System.out.println(wr);

            wr = weatherFetcher.fetchCurrentWeatherForCity("Kangaroo Valley");
            System.out.println(wr);

            wr = weatherFetcher.fetchCurrentWeatherForCity("Val’Quirico");
            System.out.println(wr);

        } catch (Exception e) {
            System.err.println("Error fetching weather: " + e.getMessage());
            e.printStackTrace();
            allFetched = false;
        }

        SuggestedLocation sl = SuggestedLocation.KANGAROO_VALLEY;
        System.out.println(sl);

        //then
        assertTrue(allFetched);
    }
}
