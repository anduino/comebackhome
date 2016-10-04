package com.example.graduation.graduationproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AirconActivity extends Activity {


    A2S_Thread m2s;
    S2A_Thread s2m;
    S2A_Thread tempThread;
    Handler handler;

    TextView tv;
    TextView statusView;
    TextView tempView,tempDateView,onTimeView;

    ControlInfo airconInfo,prevInfo,tempInfo;

    int state = -1;
    // 0 -> off
    // 1 -> on

    private static final String AIRCON_CONTROL_ON = "1";
    private static final String AIRCON_CONTROL_OFF = "0";

    private static int MSG_FROM_CONTROL = 0;
    private static int MSG_FROM_STATUS = 1;
    private static int TEMP_FROM_STATUS = 5;
    private static int  FAILED_TEMPERATURE = -2;

    ProgressDialog pdlg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aircon_activity);

        initApp();

        pdlg.show();
        s2m.getData();
        tempThread.getData();
    }

    private void initApp() {

        pdlg = new ProgressDialog(this);
        pdlg.setMessage("Please wait few seconds.");
        pdlg.setCancelable(false);
        pdlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        tempView = (TextView)findViewById(R.id.temp);
        tempDateView = (TextView)findViewById(R.id.tempDate);
        statusView = (TextView)findViewById(R.id.aircon_status);
        onTimeView = (TextView)findViewById(R.id.ontime);

        prevInfo = new ControlInfo();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.what == FAILED_TEMPERATURE)
                {
                    Toast.makeText(getApplicationContext(),"Try again please.",Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                if(msg.what == MSG_FROM_CONTROL) // M2S -> 제어 명령
                {
                    //Toast.makeText(getApplicationContext(), "complete", Toast.LENGTH_SHORT).show();
                    s2m.getData();// 바뀐 정보 UI에 반영
                }else if (msg.what == MSG_FROM_STATUS) // S2M -> 기기의 상태 확인
                {
                    // MAIN UI 변경
                    airconInfo = s2m.getControlInfo();
                    if(airconInfo.getSdate() != null) {
                        if (airconInfo.getSstatus().contains(AIRCON_CONTROL_ON)) {

                            state = 1;
                            statusView.setBackgroundResource(R.drawable.btn_2_on);

                            // 현재 시간과 on이 된 시간을 비교해서 지속시간 계산
                            Date date = null;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                date = sdf.parse(airconInfo.getSdate());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(date != null) {
                                long millis  = System.currentTimeMillis() - date.getTime();
                                int hour = ((int) millis/(1000*60*60))%24;
                                int min = ((int) millis/(1000*60))%60;
                                onTimeView.setText( String.valueOf(hour) + "h " + String.valueOf(min)+"m");
                            }else {
                                onTimeView.setText("");
                            }


                        } else if (airconInfo.getSstatus().contains(AIRCON_CONTROL_OFF)) {
                            state = 0;
                            statusView.setBackgroundResource(R.drawable.btn_2_off);
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = null;

                        try {
                            date = sdf.parse(airconInfo.getSdate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String stringDate = format.format(date);

                        tempDateView.setText("Last control : "+ stringDate);
                    }
                } else if( msg.what == TEMP_FROM_STATUS) //온도가 들어왔을 때
                {
                    tempInfo = tempThread.getControlInfo();
                    if(tempInfo.getSstatus() != null)
                    {
                        tempView.setText(String.valueOf(tempInfo.getSstatus()) + getResources().getString(R.string.cel));
                        pdlg.dismiss();

                    }
                }
            }
        };

        m2s = new A2S_Thread(handler, getApplicationContext());

        s2m = new S2A_Thread(handler, "aircon",1);
        s2m.setContext(getApplicationContext());

        tempThread = new S2A_Thread(handler, "temperature", 5);
        tempThread.setContext(getApplicationContext());
    }

    public void airconBtnClick(View v) {
        int id = v.getId();

        switch(id)
        {
            case R.id.aircon_status:
                m2s.reset();
                if(state == 1) {
                    m2s.setControl("aircon", AIRCON_CONTROL_OFF);
                    state = 0;
                }else{
                    m2s.setControl("aircon", AIRCON_CONTROL_ON);
                    state  = 1;
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
