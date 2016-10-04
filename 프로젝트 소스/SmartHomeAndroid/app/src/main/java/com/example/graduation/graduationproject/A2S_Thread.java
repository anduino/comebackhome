package com.example.graduation.graduationproject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 2015-09-11.
 */
public class A2S_Thread {

    private String siteURL = "http://203.252.182.96:5000/set_control/";

    private String controlName = null;
    private String controlNumber = null;



    private Double userPosLat = null;
    private Double userPosLng = null;


    private String phoneState = null;
    TelephonyManager tm;


    LocationManager manager;


    HttpThread thread;

    Handler handler;
    Context context;

    int unregist = 0;

    final static int UNREGISTERED = -1;
    final static int CONTROL_MSG = 0;



    //생성자
    public A2S_Thread() {

    }

    public A2S_Thread(Handler _handler, Context _context) {
        this.handler = _handler;
        this.context = _context;

    }


    public void reset() {
        siteURL = "http://203.252.182.96:5000/set_control/";
    }


    public void setUserPos() {

        if (manager == null) {
            manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        boolean nw = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = manager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            userPosLat = lastKnownLocation.getLatitude();
            userPosLng = lastKnownLocation.getLongitude();

        }
        this.siteURL = this.siteURL  + String.valueOf(userPosLat) + "/" + String.valueOf(userPosLng) + "/";
    }

    public void setControl(String name, String number) {

        this.controlName = name;
        this.controlNumber = number;
        this.siteURL = this.siteURL + name + "/" + number + "/";

        this.setUserPos();
        this.setAndId();
    }

    public void setAndId() {

        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.siteURL = this.siteURL + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }



    public String getSiteURL() {
        return this.siteURL;
    }





    public String downloadUrl(String myurl) throws IOException {
        // M2S_PACK으 로 부터 URL을 만든 후, 웹서버로 GET 방식을 이용하여 데이터를 전송
        HttpURLConnection conn = null;

        URL url = new URL(myurl);
        conn = (HttpURLConnection) url.openConnection();

        BufferedInputStream buf = new BufferedInputStream(conn.getInputStream());
        BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf, "utf-8"));

        String line = null;
        String page = "";

        while ((line = bufreader.readLine()) != null)
        {
            page += line;
        }
        return page;
    }

    public void transmitData(){
        // 네트워크 작업을 할 thread 초기화하고 작업 실행

        thread = null;
        thread = new HttpThread();
        thread.start();

    }



    class HttpThread extends Thread {
        //네트워크 작업을 할 thread
        @Override
        public void run() {
            super.run();
            try {

                String result = downloadUrl(getSiteURL());
                if(result.contains("Unregistered"))//등록되지 않은 기기
                {
                    handler.sendEmptyMessage(UNREGISTERED);
                    return;
                }

                handler.sendEmptyMessage(CONTROL_MSG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
