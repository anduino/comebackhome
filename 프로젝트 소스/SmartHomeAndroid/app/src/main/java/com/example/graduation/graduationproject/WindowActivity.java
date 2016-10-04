package com.example.graduation.graduationproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WindowActivity extends Activity {


    A2S_Thread m2s;
    S2A_Thread s2m;

    Handler handler;

    TextView statusView;
    TextView tempDateView;

    ControlInfo windowInfo;

    int state = -1;
    // 0 -> off
    // 1 -> on

    private static final String WINDOW_CONTROL_OPEN = "1";
    private static final String WINDOW_CONTROL_CLOSE = "0";

    private static int MSG_FROM_CONTROL = 0;
    private static int MSG_FROM_STATUS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_activity);
        initWindowActivity();
        s2m.getData();
    }

    protected void initWindowActivity()
    {

        statusView = (TextView)findViewById(R.id.window_status);
        tempDateView = (TextView)findViewById(R.id.tempDate);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                //Toast.makeText(getApplicationContext(),String.valueOf(msg.what),Toast.LENGTH_LONG).show();
                if(msg.what == MSG_FROM_CONTROL)
                {
                    //Toast.makeText(getApplicationContext(),"control complete",Toast.LENGTH_LONG).show();
                    s2m.getData();
                }else if (msg.what == MSG_FROM_STATUS)
                {
                    // MAIN UI ??
                    windowInfo = s2m.getControlInfo();
                    if(windowInfo.getSdate() != null) {
                        if (windowInfo.getSstatus().contains(WINDOW_CONTROL_OPEN)) {

                            state = 1;
                            statusView.setBackgroundResource(R.drawable.btn_2_open);


                        } else if (windowInfo.getSstatus().contains(WINDOW_CONTROL_CLOSE)) {
                            state = 0;
                            statusView.setBackgroundResource(R.drawable.btn_2_close);
                        }


                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = null;

                        try {
                            date = sdf.parse(windowInfo.getSdate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String stringDate = format.format(date);

                        tempDateView.setText("Last control : "+ stringDate);
                    }
                }
            }
        };

        m2s = new A2S_Thread(handler, getApplicationContext());

        s2m = new S2A_Thread(handler, "window", 2);
        s2m.setContext(getApplicationContext());

    }


    public void windowBtnClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.backBtn:
                finish();
                break;
            case R.id.window_status:
                m2s.reset();
                if(state == 1) {
                    m2s.setControl("window", WINDOW_CONTROL_CLOSE);
                    state = 0;
                }else{
                    m2s.setControl("window", WINDOW_CONTROL_OPEN);
                    state =1;
                }
                m2s.transmitData();
                break;
        }
    }

    public void topButtonClick(View v)
    {
        int id = v.getId();
        switch(id)
        {
            case R.id.topAirconBtn:
                Intent intent_aircon = new Intent(WindowActivity.this, AirconActivity.class);
                startActivity(intent_aircon);
                finish();
                break;
            case R.id.topWindowBtn:
                Intent intent_window = new Intent(WindowActivity.this,WindowActivity.class);
                startActivity(intent_window);
                finish();
                break;
            case R.id.topLampBtn:
                Intent intent_lamp = new Intent(WindowActivity.this,LampActivity.class);
                startActivity(intent_lamp);
                finish();
                break;
        }
    }

}
