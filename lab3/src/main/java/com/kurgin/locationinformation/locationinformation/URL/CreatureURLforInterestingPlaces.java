package com.kurgin.locationinformation.locationinformation.URL;

import org.apache.http.client.utils.URIBuilder;

import java.net.URL;

public class CreatureURLforInterestingPlaces {
    private URL url;
    private final String UNIQUE_API_KEY = "5ae2e3f221c38a28845f05b620a9c3e7596e9bbcb65341184c63181f";
    private final String OBJECT_CATEGORY_PARAMETR = "kinds";
    private final String OBJECT_CATEGORY = "churches";

    public URL getUrl() {
        return url;
    }

    public CreatureURLforInterestingPlaces(Double lon, Double lat) {
        try {
            String startURL = "http://api.opentripmap.com/0.1/ru/places/bbox";
            URIBuilder uriBuilder = new URIBuilder(startURL);
            double nearMinLon = lon - 0.5;
            double nearMaxLon = lon + 0.5;
            double nearMinLat = lat - 0.5;
            double nearMaxLat = lat + 0.5;
            String lon_min = Double.toString(nearMinLon);
            String lon_max = Double.toString(nearMaxLon);
            String lat_min = Double.toString(nearMinLat);
            String lat_max = Double.toString(nearMaxLat);
            uriBuilder.addParameter("lon_min", lon_min);
            uriBuilder.addParameter("lat_min", lat_min);
            uriBuilder.addParameter("lon_max", lon_max);
            uriBuilder.addParameter("lat_max", lat_max);
            uriBuilder.addParameter(OBJECT_CATEGORY_PARAMETR, OBJECT_CATEGORY);
            uriBuilder.addParameter("format", "geojson");
            uriBuilder.addParameter("apikey", UNIQUE_API_KEY);
            this.url = uriBuilder.build().toURL();
        } catch (Exception error) {
            error.printStackTrace();
        }


    }
}
