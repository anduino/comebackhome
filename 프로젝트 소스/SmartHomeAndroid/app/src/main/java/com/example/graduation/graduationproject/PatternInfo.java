package com.example.graduation.graduationproject;

/**
 * Created by KIMYEACHAN on 2016-05-19.
 */
public class PatternInfo {
    private int num;
    private double lat ;
    private double lng;
    private String time;
    private float temp;
    private int status;
    PatternInfo()
    {

    }

    PatternInfo(int num, double lat, double lng, String time, float  temp, int status) {
        this.num = num;
        this.lat = lat;
        this.lng = lng;
        this.time = time;
        this.temp = temp;
        this.status = status;
    }

    void setNum(int num) { this.num = num;}
    void setLat(double lat) { this.lat = lat;}
    void setLng(double lng) { this.lng = lng;}
    void setTime(String time) { this.time = time;}
    void setTemp(float temp) { this.temp = temp;}
    void setStatus(int status) {this.status = status;}

    int getNum() { return this.num;}
    double getLat() { return this.lat;}
    double getLng() { return this.lng;}
    String getTime() { return this. time;}
    float getTemp() { return this.temp;}
    int getStatus() { return this.status;}


}
