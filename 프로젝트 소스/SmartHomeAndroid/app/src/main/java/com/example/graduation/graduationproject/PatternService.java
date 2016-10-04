package com.example.graduation.graduationproject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;


public class PatternService extends Service implements Runnable {


    LocationManager manager;
    A2S_Thread m2s;

    SQLiteDatabase db;
    String dbName = "anduino.db";
    String tableName = "patterns";
    ArrayList<PatternInfo> list;

    final static double DIST_RADIUS = 0.05;// per Km
    private static final String AIRCON_CONTROL_ON = "1";
    private static final String AIRCON_CONTROL_OFF = "0";

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    public void getPatternDB()
    {
        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("select * from patterns where status = 1;",null);

        while(c.moveToNext())
        {
            PatternInfo p = new PatternInfo();
            p.setNum(c.getInt(c.getColumnIndex("num")));
            p.setLat(c.getDouble(c.getColumnIndex("lat")));
            p.setLng(c.getDouble(c.getColumnIndex("lng")));
            p.setTemp(c.getFloat(c.getColumnIndex("temp")));
            p.setTime(c.getString(c.getColumnIndex("time")));
            list.add(p);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        m2s = new A2S_Thread(null, this);
        list = new ArrayList<>();

        getPatternDB();
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    double getDistance(double lat1, double lng1, double lat2, double lng2) {

        double radiusKms = 6371;
        double dToR = (Math.PI / 180.0);

        lat1 = lat1 * dToR;
        lng1 = lng1 * dToR;
        lat2 = lat2 * dToR;
        lng2 = lng2 * dToR;

        double latitude = (lat2 - lat1);
        double longitude = (lng2 - lng1);

        double a = Math.pow(Math.sin(latitude / 2.0), 2.0)
                + Math.cos(lat1)
                * Math.cos(lat2)
                * Math.pow(Math.sin(longitude / 2.0), 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return c * radiusKms;
    }


    @Override
    public void run() {
        int i = 0;
        Log.v("and_check", "check on");
        if (manager == null) {
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            boolean nw = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            Log.v("and_check", "network : " + String.valueOf(nw));
//            Log.v("and_check", "GPS : " + String.valueOf(gps));
        }

        while (i < 5) {


            String locationProvider = LocationManager.NETWORK_PROVIDER;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }


            Location lastKnownLocation = manager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                double lat = lastKnownLocation.getLatitude();
                double lng = lastKnownLocation.getLongitude();

                for(int idx = 0; idx < list.size(); idx++)
                {
                    double dist = getDistance(lat,lng,list.get(idx).getLat(),list.get(idx).getLng());
                    int hour = Integer.valueOf(new java.text.SimpleDateFormat("HH").format(new java.util.Date()));
                    int min = Integer.valueOf(new java.text.SimpleDateFormat("mm").format(new java.util.Date()));

                    int value = (hour*60 + min)/30;

                    Log.v("and_dist",String.valueOf(dist));
                    Log.v("and_time",String.valueOf(hour)+ " : " + String.valueOf(min));
                    Log.v("and_user_pos", String.valueOf(lat) + " , " + String.valueOf(lng));

                    if( dist < DIST_RADIUS)
                    {
                        if( value == Integer.valueOf(list.get(idx).getTime()) )
                        {
                            // 자동 제어 수행
                            m2s.setControl("aircon", AIRCON_CONTROL_ON);
                            m2s.transmitData();
                        }
                    }
                }
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }//WHILE LOOP
    }
}
