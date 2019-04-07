package com.mrswagbhinav.trapapp;

import java.util.Date;

public class Trap {

    private String name;
    private String host;
    private Double lat;
    private Double lng;
    private Date date;

    public Trap (String name, String host, Double lat, Double lng, Date date) {
        this.name = name;
        this.host = host;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public Double getLongitude() {
        return lng;
    }

    public Double getLatitude() {
        return lat;
    }

    public Date getDate() {
        return date;
    }


}
