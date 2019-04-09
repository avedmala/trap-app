package com.mrswagbhinav.trapapp;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class Trap {

    private String title;
    private String host;
    //private ArrayList<String> commits;
    private String locationName;
    private String locationAddress;
    private Timestamp timestamp;

    public Trap (String title, String host, String locationName, String locationAddress, Timestamp timestamp) {
        this.title = title;
        this.host = host;
        //this.commits = commits;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String str) {
        locationName = str;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String str) {
        locationAddress = str;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp ts) {
        timestamp = ts;
    }


}
