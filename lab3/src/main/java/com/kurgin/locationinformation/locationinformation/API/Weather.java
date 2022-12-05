package com.kurgin.locationinformation.locationinformation.API;

import lombok.Setter;

public class Weather {
    @Setter
    private String weather;
    @Setter
    private String description;
    @Setter
    private Double temp;
    @Setter
    private Double feelsLike;

    public void printWeather() {
        System.out.println(this.weather);
        System.out.println(this.description);
    }

    public void printTemperature() {
        System.out.println(this.temp);
        System.out.println(this.feelsLike);
    }


}