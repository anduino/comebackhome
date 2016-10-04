package com.example.graduation.graduationproject;

import android.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Jihae on 2016-05-25.
 */
public class InitialActivity extends Activity implements OnMapReadyCallback {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private SharedPreferences initSetting;
    private BroadcastReceiver tokenReceiver;

    private EditText uname;
    private Button btn;
    private TextView tv;

    private String instanceID;

    SQLiteDatabase db;
    String dbName = "anduino.db";
    String tableName = "user";

    MapFragment mapFr;
    GoogleMap map;
    UiSettings uiSettings;

    LocationManager manager;
    double myLat,myLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.initial_activity);

        mapFr = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFr.getMapAsync(this);

        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE,null);

        uname = (EditText)findViewById(R.id.uname);
        initSetting = getSharedPreferences("initSetting", 0);

        btn = (Button)findViewById(R.id.registerBtn);
        btn.setOnClickListener(new View.OnClickListener() {//register btn click
            @Override
            public void onClick(View v) {
                // username 가져오기
                String userName = getUname();
                db.execSQL("insert into "+tableName+"(name,lat,lng) values('"+userName+"',"+myLat+","+myLng+");");

                // aid 가져오기
                String aId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                // 추가하기~

                // 웹서버로 보내기
                SendPost sendThread = new SendPost(aId, userName, instanceID, myLat, myLng);
                sendThread.start();

                // preference에 써주기
                SharedPreferences.Editor editor;
                editor = initSetting.edit();

                editor.putString("userName", userName);
                editor.putString("aID", aId);
                editor.commit();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
        tv = (TextView)findViewById(R.id.waitTv) ;

        // receiver 등록
        registBroadcastReceiver();


        // Instance ID 토큰 가져오기
        getInstanceIdToken();



    }


    /**
     * Instance ID를 이용하여 디바이스 토큰을 가져오는 RegistrationIntentService를 실행한다.
     */
    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:

                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * LocalBroadcast 리시버를 정의한다. 토큰을 획득하기 위한 READY, GENERATING,
     * COMPLETE 액션에 따라 UI에 변화를 준다.
     */
    public void registBroadcastReceiver(){
        tokenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();


                if(action.equals(QuickstartPreferences.REGISTRATION_READY)){
                    // 액션이 READY일 경우
                } else if(action.equals(QuickstartPreferences.REGISTRATION_GENERATING)){
                    // 액션이 GENERATING일 경우
                } else if(action.equals(QuickstartPreferences.REGISTRATION_COMPLETE)){
                    // 액션이 COMPLETE일 경우
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    instanceID = intent.getStringExtra("token");
                    btn.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.GONE);


                }

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(tokenReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_READY));
        LocalBroadcastManager.getInstance(this).registerReceiver(tokenReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_GENERATING));
        LocalBroadcastManager.getInstance(this).registerReceiver(tokenReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

    }

    /**
     * 앱이 화면에서 사라지면 등록된 LocalBoardcast를 모두 삭제한다.
     */
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tokenReceiver);
        super.onPause();
    }


    public String getUname(){
        return uname.getText().toString();
    }


    void btnClick(View v){
//
//        // username 가져오기
//        String userName = getUname();
//
//        // aid 가져오기
//        String aId = "test";
//        // 추가하기~
//
//
//        // 웹서버로 보내기
//        SendPost sendThread = new SendPost(aId, userName, instanceID);
//        sendThread.start();
//
//        // preference에 써주기
//        SharedPreferences.Editor editor;
//        editor = initSetting.edit();
//
//        editor.putString("userName", userName);
//        editor.putString("aID", aId);
//        editor.commit();
    }





    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("InitialActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        if( (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
        {
            map.setMyLocationEnabled(true);
        }

        uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        if (manager == null) { // location manager object
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }


        String locationProvider = LocationManager.NETWORK_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LatLng myLoc = null;
        Location lastKnownLocation = manager.getLastKnownLocation(locationProvider);//get my location
        if (lastKnownLocation != null) {
            double lat = lastKnownLocation.getLatitude();
            double lng = lastKnownLocation.getLongitude();
            myLat = lat; myLng = lng;

            myLoc = new LatLng(lat,lng);
        }

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 18));
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                myLat = latLng.latitude;
                myLng = latLng.longitude;

                map.clear();

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)); // BitmapDescriptorFactory.fromResource(R.drawable.station))
                map.addMarker(options);
                map.addCircle(new CircleOptions()
                        .center(new LatLng(latLng.latitude, latLng.longitude))
                        .radius(25)
                        .strokeColor(Color.parseColor("#000000ff"))
                        .fillColor(Color.parseColor("#5587cefa")));

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                Toast.makeText(getApplicationContext(),"If you want to choose this place as your home, click the below button.",Toast.LENGTH_LONG).show();
            }
        });


    }

    class SendPost extends Thread {
        String aID = null;
        String uName = null;
        String tokenID = null;
        double lat = 0.0;
        double lng = 0.0;

        SendPost(String aID, String uName, String tokenID, double lat, double lng){
            this.aID = aID;
            this.uName = uName;
            this.tokenID = tokenID;
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        public void run() {
            super.run();


            ContentValues values=new ContentValues();
            values.put("uname",uName);

            String u = "http://203.252.182.96:5000/register";

            StringBuilder sb = new StringBuilder();
            try {
                sb.append(URLEncoder.encode("uname", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(uName, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            String query = "aid="+aID+"&uname="+uName+"&token="+tokenID+"&lat="+lat+"&lng="+lng;

            try {
                URL url = new URL(u);
                URLConnection connection = url.openConnection();

                HttpURLConnection hurlc = (HttpURLConnection)connection;

                hurlc.setRequestMethod("POST");
                hurlc.setReadTimeout(10000);
                hurlc.setConnectTimeout(15000);
                hurlc.setDoOutput(true);
                hurlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                hurlc.setUseCaches(false);
                hurlc.setDefaultUseCaches(false);



                OutputStream out = hurlc.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(query/*.getBytes("euc-kr")*/);
                writer.flush();
                writer.close();
                out.close();

                if (hurlc.getResponseCode() == HttpURLConnection.HTTP_OK) { // 연결에 성공한 경우
                    String line;
                    //BufferedReader br = new BufferedReader(new InputStreamReader(hurlc.getInputStream())); // 서버의 응답을 읽기 위한 입력 스트림

                    // preference에 써주기
                    SharedPreferences.Editor editor;
                    editor = initSetting.edit();

                    editor.putBoolean("isSet", true);
                    editor.commit();

                }

                hurlc.disconnect();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
