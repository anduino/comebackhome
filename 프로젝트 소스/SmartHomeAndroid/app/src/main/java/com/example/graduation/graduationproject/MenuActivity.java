package com.example.graduation.graduationproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MenuActivity extends Activity {

    String dbName = "anduino.db";
    String tableNamePattern = "patterns";
    String tableNameUser = "user";
    SQLiteDatabase db;

    S2A_Thread[] s2m;
    Handler handler;

    final static int AIRCON_STATE = 1;
    final static int WINDOW_STATE = 2;
    final static int LAMP_STATE = 3;
    final static int UNREGISTERED = -1;

    TextView airconView, windowView, lampView;
    int isDialogShow = 0;


    @Override
    protected void onResume() {
        super.onResume();

        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE,null);

        Cursor c = db.rawQuery("select * from user;",null);
        if(c.getCount() == 1)
        {
            for (int i = 0; i < s2m.length; i++) {
                s2m[i].getData();
            }
        }

//        initSetting = getSharedPreferences("initSetting", 0);
//        if (initSetting.getBoolean("isSet", false)) { //등록된 경우
//
//                for (int i = 0; i < s2m.length; i++) {
//                    s2m[i].getData();
//                }
//        }
    }

    private SharedPreferences initSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        // database create
        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        //if not exists
        String createSQL = "create table if not exists " + tableNamePattern +
                "(_id integer primary key autoincrement, lat double, lng double, time text, " +
                "temp float, status int, num int, name text)";
        db.execSQL(createSQL);

        createSQL = "create table if not exists " + tableNameUser +
                "(_id integer primary key autoincrement, name text, lat double, lng double)";
        db.execSQL(createSQL);


        checkInitial();
        initMenuActivity();
        startServiceMethod();

    }

    private void checkInitial() {
        initSetting = getSharedPreferences("initSetting", 0);
        if (!initSetting.getBoolean("isSet", false)) {
            // 앱이 처음으로 실행되는 경우 설정 액티비티 실행
            Intent intent_initial = new Intent(MenuActivity.this, InitialActivity.class);
            startActivity(intent_initial);
        }
    }


    private void initMenuActivity() {

        airconView = (TextView) findViewById(R.id.aircon_view);
        windowView = (TextView) findViewById(R.id.window_view);
        lampView = (TextView) findViewById(R.id.lamp_view);



        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if (msg.what == UNREGISTERED) {

                    if(isDialogShow == 0 ) {
                        Intent dialog = new Intent(MenuActivity.this, UnregisteredDialog.class);
                        startActivity(dialog);
                        isDialogShow = 1;
                    }
                    return;
                }

                if (msg.what == AIRCON_STATE) {
                    ControlInfo info = s2m[0].getControlInfo();
                    if (info.getSstatus() != null) {
                        String status = info.getSstatus();
                        if (status.contains("0"))//off
                        {
                            airconView.setBackgroundResource(R.drawable.icon_air_off);
                        } else {//on
                            airconView.setBackgroundResource(R.drawable.icon_air_on);
                        }
                    }
                } else if (msg.what == WINDOW_STATE) {
                    ControlInfo info = s2m[1].getControlInfo();
                    if (info.getSstatus() != null) {
                        String status = info.getSstatus();
                        if (status.contains("0"))//closed
                        {
                            windowView.setBackgroundResource(R.drawable.icon_window_closed);
                        } else {//open
                            windowView.setBackgroundResource(R.drawable.icon_window_opened);
                        }

                    }

                } else if (msg.what == LAMP_STATE) {
                    ControlInfo info = s2m[2].getControlInfo();
                    if (info.getSdate() != null) {
                        String status = info.getSstatus();
                        Log.v("and_lamp_s",status);
                        if (status.contains("0"))//off
                        {
                            lampView.setBackgroundResource(R.drawable.icon_lamp_off);
                        } else {//on
                            lampView.setBackgroundResource(R.drawable.icon_lamp_on);
                        }

                    }
                }//end status

            }
        };

        s2m = new S2A_Thread[3];
        /*
        s2m의 인덱스는 제어할 기기의 인덱스
        0 -> aircon의 정보 관리
         */
        s2m[0] = new S2A_Thread(handler, "aircon", AIRCON_STATE);
        s2m[0].setContext(getApplicationContext());

        s2m[1] = new S2A_Thread(handler, "window", WINDOW_STATE);
        s2m[1].setContext(getApplicationContext());

        s2m[2] = new S2A_Thread(handler, "lamp", LAMP_STATE);
        s2m[2].setContext(getApplicationContext());

        Cursor c = db.rawQuery("select * from user;",null);
        //Toast.makeText(getApplicationContext(),String.valueOf(c.getCount()),Toast.LENGTH_LONG).show();
        if(c.getCount() != 0) {
            for (int i = 0; i < s2m.length; i++) {
                s2m[i].getData();
            }
        }

    }

    public void startServiceMethod() {

        Intent Service = new Intent(this, PatternService.class);
        startService(Service);
    }

    public void menuClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.menu_aircon:
                Intent intent_aircon = new Intent(MenuActivity.this, AirconActivity.class);
                startActivity(intent_aircon);
                break;
            case R.id.menu_window:
                Intent intent_window = new Intent(MenuActivity.this, WindowActivity.class);
                startActivity(intent_window);
                break;
            case R.id.menu_lamp:
                Intent intent_lamp = new Intent(MenuActivity.this, LampActivity.class);
                startActivity(intent_lamp);
                break;
            case R.id.menu_pattern:
                Intent intent_pattern = new Intent(MenuActivity.this, PatternActivity.class);
                startActivity(intent_pattern);
                break;
            case R.id.menu_setting:
                Intent intent_setting = new Intent(MenuActivity.this, SettingActivity.class);
                startActivity(intent_setting);
                break;
        }
    }


    public void btnClick(View v) {
        int id = v.getId();

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


}
