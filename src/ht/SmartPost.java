/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ht;

import java.util.ArrayList;

/**
 *
 * @author mikko
 */
public class SmartPost {
    private int spid;
    private String name;
    private String availability;
    private String lat;
    private String lon;
    private String address;
    private String zip;
    private String city;
    
    public SmartPost(int s, String n, String av, String la, String lo, String ad, String z, String c) {
        spid = s;
        name = n;
        availability = av;
        lat = la;
        lon = lo;
        address = ad;
        zip = z;
        city = c;
    }

    public int getSpid() {
        return spid;
    }

    public String getName() {
        return name;
    }

    public String getAvailability() {
        return availability;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getAddress() {
        return address;
    }

    public String getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }
}
