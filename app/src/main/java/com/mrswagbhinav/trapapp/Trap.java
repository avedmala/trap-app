package com.mrswagbhinav.trapapp;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class Trap {

    private String title;
    private String host;
    //private ArrayList<String> commits;
    private GeoPoint geoPoint;
    private Timestamp timestamp;

    public Trap (String title, String host, GeoPoint geoPoint, Timestamp timestamp) {
        this.title = title;
        this.host = host;
        //this.commits = commits;
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String str) {
        title = str;
    }

    public String getHost() {
        return host;
    }

//    public ArrayList<String> getCommits() {
//        return commits;
//    }
//
//    public void addCommit(String user) {
//        commits.add(user);
//    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint gp) {
        geoPoint = gp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp ts) {
        timestamp = ts;
    }


}
