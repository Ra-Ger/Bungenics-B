package com.kodilla.bungenics.dataFetchers.OpenMeteo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SuggestedLocation {
    // Islandia
    STOKKSEYRI("Stokkseyri", "Iceland",
            63.8350, -21.0620,
            "Subpolar oceanic",
            "Cool summers (10-15°C), cold winters (0-5°C), windy, frequent rain and snow",
            "Windy, humid, with frequent precipitation. Summers are short and cool.",
            "Icelandic coastal village, known for its Viking history and stunning landscapes"),

    // Wielka Brytania
    HAMBLEDEN("Hambleden", "United Kingdom",
            51.5700, -0.8700,
            "Temperate oceanic",
            "Mild summers (18-22°C), cool winters (5-10°C), frequent rain throughout the year",
            "Moderate temperatures, high humidity, frequent overcast and rain",
            "Charming English village in the Chiltern Hills, surrounded by beech woods"),

    // Polska
    PECICE("Pęcice", "Poland",
            52.1397, 20.8556,
            "Humid continental",
            "Warm summers (20-25°C), cold winters (-5 to 0°C), snow in winter, rain in summer",
            "Four distinct seasons, cold snowy winters, warm rainy summers",
            "Rural village near Warsaw, surrounded by forests and agricultural land"),

    // Rosja
    PROKSHINO("Prokshino", "Russia",
            56.5000, 38.5000,
            "Continental",
            "Warm summers (18-23°C), very cold winters (-10 to -5°C), heavy snowfall",
            "Long cold winters, short warm summers, significant temperature swings",
            "Small Russian village in the Vladimir Oblast, typical of the Russian countryside"),

    // Egipt
    SAQQARA("Saqqara", "Egypt",
            29.8710, 31.2160,
            "Hot desert",
            "Very hot summers (35-40°C), mild winters (15-20°C), almost no rain, dry",
            "Extremely dry with intense sunshine, large daily temperature variations",
            "Ancient Egyptian village near the famous Step Pyramid of Djoser"),

    // Chiny
    CUANDIXIA("Cuandixia", "China",
            40.0000, 116.0000,
            "Humid continental",
            "Hot rainy summers (25-30°C), cold dry winters (-5 to 0°C), monsoon rains in summer",
            "Four seasons, hot and humid summers, cold winters with little snow",
            "Ancient mountain village near Beijing, known for its traditional courtyard houses"),

    // Australia
    KANGAROO_VALLEY("Kangaroo Valley", "Australia",
            -34.7200, 150.5300,
            "Humid subtropical",
            "Warm humid summers (25-30°C), mild winters (15-20°C), moderate rainfall",
            "Mild and humid, with occasional summer storms and foggy winters",
            "Picturesque valley in New South Wales, famous for its lush green hills and wildlife"),

    // Meksyk
    VAL_QUIRICO("Val'Quirico", "Mexico",
            19.2800, -98.2300,
            "Subtropical highland",
            "Mild year-round (20-25°C), rainy season in summer (Jun-Sep), dry winters",
            "Mild and pleasant year-round, cool nights, rainy summers",
            "Tuscan-style village near Puebla, built around a former monastery"),;

    private final String name;
    private final String country;
    private final double latitude;
    private final double longitude;
    private final String climateType;
    private final String typicalWeather;
    private final String typicalConditions;
    private final String description;

    public String getDisplayName() {
        return name + ", " + country;
    }

    @Override
    public String toString() {
        return String.format("%s | Climate: %s | Conditions: %s | Description: %s",
                getDisplayName(), climateType, typicalConditions, description);
    }
}
