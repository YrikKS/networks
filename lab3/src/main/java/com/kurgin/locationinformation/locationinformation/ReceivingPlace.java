package com.kurgin.locationinformation.locationinformation;

import com.kurgin.locationinformation.locationinformation.API.Place;
import com.kurgin.locationinformation.locationinformation.URL.CreatureURLforPlace;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ReceivingPlace {
    private List<String> listPlaces;
    JSONObject jsonObject;
    @Getter private  ArrayList<Place> placeArrayList;
    public ReceivingPlace(String str) {
        try {
            listPlaces = new ArrayList<String>();
            CreatureURLforPlace creatureURl = new CreatureURLforPlace("https://graphhopper.com/api/1/geocode", str);
            URL url = creatureURl.getUrl();
            URLtoString urLtoString = new URLtoString(url);
            String s = urLtoString.urlToString();
            this.jsonObject = (JSONObject) JSONValue.parseWithException(s);
            placeArrayList = new ArrayList<Place>();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public void setListPlaces() {
        JSONArray jsonArray = (JSONArray) jsonObject.get("hits");
        for (Object it : jsonArray) {
            JSONObject j = (JSONObject) it;
            Place place = new Place();
            place.setPlaceName((String) j.get("name"));
            place.setCountry((String) j.get("country"));
            JSONObject cord = (JSONObject) j.get("point");
            place.setLatitude((Double) cord.get("lat"));
            place.setLongitude((Double) cord.get("lng"));
            listPlaces.add(place.placeToString());
            placeArrayList.add(place);
        }
    }

    public void printListPlaces() {
        int count = 0;
        for (String st : listPlaces) {
            System.out.println(count + " " + st);
            ++count;
        }
    }
    public Place getPlace(int number) {
        return placeArrayList.get(number);
    }
}
