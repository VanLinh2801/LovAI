package com.example.lovai.Model;

public class Forecast {
    private String day;
    private String date;
    private String weather;
    private String suggestion;

    public Forecast(String day, String date, String weather, String suggestion) {
        this.day = day;
        this.date = date;
        this.weather = weather;
        this.suggestion = suggestion;
    }

    public String getDay() { return day; }
    public String getDate() { return date; }
    public String getWeather() { return weather; }
    public String getSuggestion() { return suggestion; }
}
