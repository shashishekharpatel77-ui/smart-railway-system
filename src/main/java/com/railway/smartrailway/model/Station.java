package com.railway.smartrailway.model;

public class Station {

    private final int stationId;
    private final String stationCode;
    private final String stationName;
    private final String city;

    public Station(int stationId, String stationCode, String stationName, String city) {
        this.stationId = stationId;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
    }

    public int getStationId() {
        return stationId;
    }

    public String getStationCode() {
        return stationCode;
    }

    public String getStationName() {
        return stationName;
    }

    public String getCity() {
        return city;
    }
}