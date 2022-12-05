package com.kurgin.locationinformation.locationinformation;

import com.kurgin.locationinformation.locationinformation.API.Weather;
import com.kurgin.locationinformation.locationinformation.URL.СreatureURL;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;

public class ReceivingWeather {
    private final String API_WEATHER = "http://api.openweathermap.org/data/2.5/weather";
    JSONObject weather;
    JSONObject jsonObject;
    @Getter private Weather getterWeather;

    public void receiveWeather(String lat, String lon) {
        try {
            СreatureURL creatureURl = new СreatureURL(API_WEATHER, lat, lon);
            URL url = creatureURl.getUrl();
            URLtoString urLtoString = new URLtoString(url);
            String s = urLtoString.urlToString();
            this.jsonObject = (JSONObject) JSONValue.parseWithException(s);
            JSONArray jsonArray = (JSONArray) jsonObject.get("weather");
            this.weather = (JSONObject) jsonArray.get(0);
            getterWeather = new Weather();
            getterWeather.setWeather((String) weather.get("main"));
            getterWeather.setDescription((String) weather.get("description"));
            JSONObject temp = (JSONObject) jsonObject.get("main");
            getterWeather.setTemp((Double) temp.get("temp"));
            getterWeather.setFeelsLike((Double) temp.get("feels_like"));
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public void getWeather(Weather weather) {
        weather.printWeather();
    }

    public void getTemperature(Weather weather) {
        weather.printTemperature();
    }
}
