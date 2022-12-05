package com.kurgin.locationinformation.locationinformation.URL;

import lombok.Getter;
import org.apache.http.client.utils.URIBuilder;

import java.net.URL;

public class CreatureURlForDescription {
    @Getter URL url;
    public CreatureURlForDescription(String xid) {
        try {
            String startURL =  "http://api.opentripmap.com/0.1/ru/places/xid/";
            String str = startURL + xid;
            URIBuilder uriBuilder = new URIBuilder(str);
            uriBuilder.addParameter("apikey", "5ae2e3f221c38a28845f05b67294f04849559e7fd11940fcc469ceb3");
            this.url = uriBuilder.build().toURL();
        }catch (Exception error) {
            error.printStackTrace();
        }

    }
}
