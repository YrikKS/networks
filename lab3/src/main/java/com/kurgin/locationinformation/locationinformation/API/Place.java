package com.kurgin.locationinformation.locationinformation.API;

import lombok.Getter;
import lombok.Setter;



public class Place {
    @Setter
    @Getter
    private String placeName;
    @Setter
    @Getter
    private String country;
    @Setter
    @Getter
    private Double latitude;
    @Setter
    @Getter
    private Double longitude;

    public String placeToString() {
        return "Place: " + placeName + " Country: " + country + " Latitude: " + latitude.toString()
                + " Longitude: " + longitude.toString();
    }
}
