package com.kurgin.locationinformation.locationinformation;

import com.kurgin.locationinformation.locationinformation.API.InterestingPlaces;
import com.kurgin.locationinformation.locationinformation.URL.CreatureURLforInterestingPlaces;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.util.ArrayList;

public class ReceivingInterestingPlaces {
    @Getter private ArrayList<InterestingPlaces> interestingPlacesArrayList;
    @Getter private  ArrayList<String> xidList;
    public void receiveInterestingPlaces(Double lon, Double lat) {
        try {
            interestingPlacesArrayList = new ArrayList<InterestingPlaces>();
            xidList = new ArrayList<String>();
            var interestingPlaces = new CreatureURLforInterestingPlaces(lon, lat);
            URL url = interestingPlaces.getUrl();
            URLtoString urLtoString = new URLtoString(url);
            String s = urLtoString.urlToString();
            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(s);
            JSONArray jsonArray = (JSONArray) jsonObject.get("features");
            for (Object it : jsonArray) {
                JSONObject j = (JSONObject) it;
                JSONObject getProperties = (JSONObject) j.get("properties");
                String name = (String) getProperties.get("name");
                JSONObject getGeometry = (JSONObject)j.get("geometry");
                JSONArray coordinates = (JSONArray) getGeometry.get("coordinates");
                var interestingPlace = new InterestingPlaces();
                interestingPlace.setName((String)name);
                interestingPlace.setLon((Double) coordinates.get(0));
                interestingPlace.setLat((Double) coordinates.get(1));
                interestingPlace.setXid((String)getProperties.get("xid"));
                xidList.add((String) getProperties.get("xid"));
                interestingPlacesArrayList.add(interestingPlace);
            }
        }catch (Exception error) {
            error.printStackTrace();
        }

    }
}
