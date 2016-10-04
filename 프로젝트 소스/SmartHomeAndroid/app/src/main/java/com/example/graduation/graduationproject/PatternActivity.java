package com.example.graduation.graduationproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class PatternActivity extends Activity implements OnMapReadyCallback{
    String dbName = "anduino.db";
    String tableName = "patterns";
    SQLiteDatabase db;

    S2A_Thread s2m;
    ListView list;
    PatternAdapter adapter;

    Handler handler;

    MapFragment mapFr;
    GoogleMap map;
    UiSettings uiSettings;
    FrameLayout fr;
    ArrayList<PatternInfo> patternList;

    ProgressDialog pdlg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern_activity);
        initPatternActivity();
    }

    private void initPatternActivity() {

        fr = (FrameLayout)findViewById(R.id.mapContainer);
        mapFr = (MapFragment)getFragmentManager().findFragmentById(R.id.map2);
        mapFr.getMapAsync(this);

        pdlg = new ProgressDialog(this);
        pdlg.setMessage("Download...");
        pdlg.setCancelable(false);
        pdlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        patternList = new ArrayList<>();

        list = (ListView)findViewById(R.id.plist);

        //db open
        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE,null);

        //listview - adapter connect
        adapter = new PatternAdapter(getApplicationContext(),null,false);
        adapter.setDB(this.db);
        list.setAdapter(adapter);

        // handler overriding
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // insert pattern data(from server) to sqlite database
                DBSetting(s2m.getPattern());

                // insert array
                patternList.clear();
                String selectSQL = "select * from "+tableName;
                Cursor cur = db.rawQuery(selectSQL,null);
                cur.moveToFirst();
                if( cur.getCount() > 0) {
                    cur.moveToFirst();
                    for (int i = 0; i < cur.getCount(); i++) {

                        PatternInfo p = new PatternInfo();
                        p.setLat(cur.getDouble(cur.getColumnIndex("lat")));
                        p.setLng(cur.getDouble(cur.getColumnIndex("lng")));
                        patternList.add(p);

                        cur.moveToNext();
                    }
                }

                pdlg.dismiss();

            }
        };

        // executes getting pattern data from server
        s2m = new S2A_Thread(handler,"pattern", 4);
        s2m.setContext(getApplicationContext());


        String selectSQL = "select * from "+tableName;
        Cursor cur = db.rawQuery(selectSQL,null);
        cur.moveToFirst();
        if( cur.getCount() > 0) {
            cur.moveToFirst();
            for (int i = 0; i < cur.getCount(); i++) {

                PatternInfo p = new PatternInfo();
                p.setLat(cur.getDouble(cur.getColumnIndex("lat")));
                p.setLng(cur.getDouble(cur.getColumnIndex("lng")));
                patternList.add(p);

                cur.moveToNext();
            }
        }

        cur = db.rawQuery(selectSQL,null);
        if( cur.getCount() > 0)
        {
            adapter.changeCursor(cur);
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                fr.setVisibility(View.VISIBLE);
                Double lat = patternList.get(position).getLat();
                Double lng = patternList.get(position).getLng();

                uiSettings = map.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);

                final LatLng Loc = new LatLng(lat, lng);

                MarkerOptions options = new MarkerOptions();
                options.position(Loc);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)); // BitmapDescriptorFactory.fromResource(R.drawable.station))
                map.addMarker(options);
                map.addCircle(new CircleOptions()
                        .center(new LatLng(lat, lng))
                        .radius(25)
                        .strokeColor(Color.parseColor("#884169e1"))
                        .fillColor(Color.parseColor("#5587cefa")));

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(Loc, 19));
            }
        });

    }

    public void DBSetting(ArrayList<PatternInfo> plist)
    {
        String deleteSql = "delete from " + tableName + ";";
        db.execSQL(deleteSql);

        if(plist.size() > 0) {
            for (int i = 0; i < plist.size(); i++) {

                String sql = "insert into " + tableName + "(lat,lng,time,temp,status,num) values(" + plist.get(i).getLat() + "," + plist.get(i).getLng() + ",'" + plist.get(i).getTime() + "', " +
                        plist.get(i).getTemp() + "," + plist.get(i).getStatus() + "," + plist.get(i).getNum() + ");";
                db.execSQL(sql);
            }
        }

        String selectSQL = "select * from "+tableName;
        Cursor cur = db.rawQuery(selectSQL,null);
        if( cur.getCount() > 0)
        {
            adapter.changeCursor(cur);
            Log.v("and_result_count",String.valueOf(cur.getCount()));
        }else {

        }
    }

    public void patternBtnClick(View v) {
        int id = v.getId();
        switch (id)
        {
            case R.id.updateBtn:
                pdlg.show();
                s2m.getData();
                break;
            case R.id.backBtn:
                finish();
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void settingOnOff(int num, int status)
    {
        String sql = "update +" +tableName+" set status ="+status+" where num ="+num+"; ";
        db.execSQL(sql);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }
}
