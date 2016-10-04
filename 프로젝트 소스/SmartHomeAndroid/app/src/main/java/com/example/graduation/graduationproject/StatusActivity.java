package com.example.graduation.graduationproject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StatusActivity extends Activity {

     S2A_Thread[] s2m;
     Handler handler;
    ControlInfo airconInfo,tempInfo;

    enum Type { aircon,lamp };
    Type CONTROL_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_activity);
        init();

    }

    public void getData()
    {
        s2m[0].getData();
        s2m[2].getData();
    }


    protected void init()
    {

        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout container = (LinearLayout)findViewById(R.id.status_container);

        // temperature
        LinearLayout contents3 = (LinearLayout)inflater.inflate(R.layout.status_view, container, false);

        TextView tempName = (TextView)contents3.findViewById(R.id.contents_name);

        tempName.setText("TEMPERATURE");

        ImageView icon3 = (ImageView)contents3.findViewById(R.id.contents_image);
        icon3.setImageResource(R.drawable.thermometer);

        final TextView tempText = (TextView)contents3.findViewById(R.id.contents_status);
        final TextView tempDate = (TextView)contents3.findViewById(R.id.contents_date);

        //aircon
        LinearLayout contents = (LinearLayout)inflater.inflate(R.layout.status_view, container, false);

        TextView airconName = (TextView)contents.findViewById(R.id.contents_name);
        airconName.setText("AIRCON");

        ImageView icon = (ImageView)contents.findViewById(R.id.contents_image);
        icon.setImageResource(R.drawable.aircon);

        final TextView airconText = (TextView)contents.findViewById(R.id.contents_status);
        final TextView airconDate = (TextView)contents.findViewById(R.id.contents_date);

        // lamp
        LinearLayout contents2 = (LinearLayout)inflater.inflate(R.layout.status_view, container, false);

        TextView lampName = (TextView)contents2.findViewById(R.id.contents_name);
        lampName.setText("LAMP");

        ImageView icon2 = (ImageView)contents2.findViewById(R.id.contents_image);
        icon2.setImageResource(R.drawable.lamp);

        TextView lampText = (TextView)contents2.findViewById(R.id.contents_status);
        final TextView lampDate = (TextView)contents2.findViewById(R.id.contents_date);


        container.addView(contents);
        container.addView(contents2);
        container.addView(contents3);

        lampText.setTextColor(Color.parseColor("#00ff00"));
        lampText.setText("ON");
        lampDate.setText("Last : " + "2016-04-27 14시 53분");

        // UI 변경을 위한 핸들러 초기화
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what)
                {
                    case 1: // aircon

                        airconInfo = s2m[0].getControlInfo();
                        if(airconInfo.getSdate() != null) {
                            if (airconInfo.getSstatus().contains("1")) {
                                airconText.setTextColor(Color.parseColor("#00ff00"));
                                airconText.setText("ON");
                            } else if (airconInfo.getSstatus().contains("0")) {
                                airconText.setTextColor(Color.parseColor("#ff0000"));
                                airconText.setText("OFF");
                            }

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = null;
                            try {
                                date = sdf.parse(airconInfo.getSdate());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH시 mm분");
                            String stringDate = format.format(date);
                            airconDate.setText("Last : "+ stringDate);


                        }

                        break;
                    case 2: // lamep
                        break;
                    case 3:  // temp
                        tempInfo = s2m[2].getControlInfo();
                        if(tempInfo.getSstatus() != null)
                        {
                            tempText.setText(String.valueOf(tempInfo.getSstatus())+" ℃");

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = null;
                            try {
                                date = sdf.parse(tempInfo.getSdate());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH시 mm분");
                            String stringDate = format.format(date);
                            tempDate.setText("Last : "+ stringDate);

                        }

                        break;
                }
            }
        };
        s2m = new S2A_Thread[3];
        /*
        s2m의 인덱스는 제어할 기기의 인덱스
        0 -> aircon의 정보 관리
         */
        s2m[0] = new S2A_Thread(handler,"aircon",1);
        s2m[2] = new S2A_Thread(handler,"temperature",3);


        getData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_status, menu);
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
