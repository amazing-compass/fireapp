package com.example.fireapp.orm;

public class LocationBean {

    //手机当前所处的纬度
    private double latitude;

    //手机当前所处的经度
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}