package com.kurgin.locationinformation.locationinformation.URL;

import org.apache.http.client.utils.URIBuilder;

import java.net.URL;

public class CreatureURLforPlace {
    private final String LOCATION_PARAMETR = "q";
    private URL url;
    public CreatureURLforPlace(String str, String place) {
        try {
            URIBuilder uriBuilder = new URIBuilder(str);
            uriBuilder.addParameter(LOCATION_PARAMETR, place);
            uriBuilder.addParameter("locale", "de");
            uriBuilder.addParameter("key", "026e2ebb-27ed-491f-8020-5550503108a3");
            this.url = uriBuilder.build().toURL();
        }catch (Exception error) {
            error.printStackTrace();
        }
    }

    public URL getUrl() {
        return this.url;
    }
}
