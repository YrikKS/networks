package com.kurgin.locationinformation.locationinformation.URL;
import org.apache.http.client.utils.URIBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class СreatureURL {
    private URL url;
    private final String UNIQUE_API_KEY = "241de9349721df959d8800c12ca4f1f3";
    private final String UNIQUE_API_KEY_PARAMETR = "appid";
    private final  String UNITS_MEASUREMENT_PARAMETR = "units";
    private final  String UNITS_MEASUREMENT = "metric";

    public СreatureURL(String str, String lat, String lon) {
        try {
            URIBuilder uriBuilder = new URIBuilder(str);
            uriBuilder.addParameter("lat", lat);
            uriBuilder.addParameter("lon", lon);
            uriBuilder.addParameter(UNITS_MEASUREMENT_PARAMETR, UNITS_MEASUREMENT);
            uriBuilder.addParameter(UNIQUE_API_KEY_PARAMETR, UNIQUE_API_KEY);
            this.url = uriBuilder.build().toURL();
        } catch (URISyntaxException | MalformedURLException error) {
            error.printStackTrace();
        }
    }

    public URL getUrl() {
        return url;
    }
}
