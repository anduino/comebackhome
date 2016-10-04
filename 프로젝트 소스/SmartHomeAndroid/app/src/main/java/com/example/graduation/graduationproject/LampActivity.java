package com.example.graduation.graduationproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Externalizable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LampActivity extends Activity {


    A2S_Thread m2s;
    S2A_Thread s2m;

    Handler handler;

    TextView statusView;
    TextView tempDateView;

    ControlInfo lampInfo;

    int state = -1;
    // 0 -> off
    // 1 -> on

    private static final String LAMP_CONTROL_ON = "1";
    private static final String LAMP_CONTROL_OFF = "0";

    private static int MSG_FROM_CONTROL = 0;
    private static int MSG_FROM_STATUS = 3;
    private static int TEMP_FROM_STATUS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lamp_activity);

        initLampActivity();
        s2m.getData();
    }

    private void initLampActivity() {

        statusView = (TextView)findViewById(R.id.lamp_status);
        tempDateView = (TextView)findViewById(R.id.tempDate);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                //Toast.makeText(getApplicationContext(), String.valueOf(msg.what), Toast.LENGTH_SHORT).show();

                if(msg.what == MSG_FROM_CONTROL) // M2S -> 제어 명령
                {
                    //Toast.makeText(getApplicationContext(), "complete", Toast.LENGTH_SHORT).show();
                    s2m.getData();// 바뀐 정보 UI에 반영
                }else if (msg.what == MSG_FROM_STATUS) // S2M -> 기기의 상태 확인
                {
                    // MAIN UI 변경
                    lampInfo = s2m.getControlInfo();
                    if(lampInfo.getSdate() != null) {
                        if (lampInfo.getSstatus().contains(LAMP_CONTROL_ON)) {

                            state = 1;
                            statusView.setBackgroundResource(R.drawable.btn_2_on);

                            // 현재 시간과 on이 된 시간을 비교해서 지속시간 계산
                            Date date = null;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                date = sdf.parse(lampInfo.getSdate());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        } else if (lampInfo.getSstatus().contains(LAMP_CONTROL_OFF)) {
                            state = 0;
                            statusView.setBackgroundResource(R.drawable.btn_2_off);
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = null;

                        try {
                            date = sdf.parse(lampInfo.getSdate());
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

        s2m = new S2A_Thread(handler, "lamp",3);
        s2m.setContext(getApplicationContext());


    }

    public void lampBtnClick(View v) {
        int id = v.getId();
        switch(id)
        {
            case R.id.lamp_status:
                m2s.reset();
                if(state == 1) {
                    m2s.setControl("lamp", LAMP_CONTROL_OFF);
                    state = 0;
                }else{
                    m2s.setControl("lamp", LAMP_CONTROL_ON);
                    state = 1;
                }
                m2s.transmitData();
                break;
            case R.id.backBtn:
                finish();
                break;
        }
    }

    public void topBtnClicK_air(View v)
    {
        int id = v.getId();
        switch(id)
        {
            case R.id.topAirconBtn:
                Intent intent_aircon = new Intent(this,AirconActivity.class);
                startActivity(intent_aircon);

                finish();
                break;
            case R.id.topWindowBtn:
                Intent intent_window = new Intent(this,WindowActivity.class);
                startActivity(intent_window);

                finish();
                break;
            case R.id.topLampBtn:
                Intent intent_lamp = new Intent(this,LampActivity.class);
                startActivity(intent_lamp);

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


}
