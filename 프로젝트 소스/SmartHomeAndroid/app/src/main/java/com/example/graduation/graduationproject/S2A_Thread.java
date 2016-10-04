package com.example.graduation.graduationproject;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by user on 2015-09-11.
 */
public class S2A_Thread {
    //서버의 주소에 GET방식으로 보낼 데이터의 URL을 관리하는 CLASS

    private Context context;

    //웹서버 URL
    private String siteURL = "http://203.252.182.96:5000/get_status/";
    private String psiteURL = "http://203.252.182.96:5000/get_pattern/";

    // 제어할 기기의 이름과 제어 명령(command number)
    // ex) controlName = aircon controlNumber = 1  => aircon을 1번 상태(끄기) 로 변경하도록 요구
    private String sname = null;
    private String sstatus = null;
    private String sdate = null;

    ControlInfo controlInfo;
    ArrayList<PatternInfo> plist;

    //네트워크 작업을 할 thread
    HttpThread thread;
    // UI 제어 핸들러
    Handler handler;

    int controlType = -1;
    int eventType;
    XmlPullParser xpp;
    XmlPullParserFactory factory;

    //생성자
    public S2A_Thread() {

    }

    public S2A_Thread(Handler _handler, String _name, int _controlType) {
        /*
        handler -> ui 변경을 위한 핸들러
        name -> 제어할 기기명
        controlType -> 핸들러로 보내기 위한 메세지 값 (1 -> aircon, 2-> lamp, 3-> temperature, 4->pattern)
         */
        this.handler = _handler;
        this.sname = _name;
        this.siteURL = this.siteURL + _name;
        this.controlType = _controlType;
        plist = new ArrayList<PatternInfo>();
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public String getSiteURL() {
        //현재 저장된 url+정보 반환
        //TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(controlType == 4)
            return this.psiteURL + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        else
            return this.siteURL + "/" + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
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
            page += (line + '\n');
        }

        return page;
    }


    public String getSname()
    {
        return sname;
    }

    public String getStatus()
    {
        return sstatus;
    }




    public void getData(){
        // 네트워크 작업을 할 thread 초기화하고 작업 실행

        thread = null;
        thread = new HttpThread();
        thread.start();

    }

    public ArrayList<PatternInfo> getPattern(){
        return this.plist;
    }


    public void parsePattern(String result){

        boolean isNum = false;
        boolean isLat = false;
        boolean isLng = false;
        boolean isTime = false;
        boolean isTemp = false;
        boolean isSet = false;

        PatternInfo info = null;
        if(plist.size() != 0)
            plist.clear();

        String tagName = null;

        try{
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result.trim()));
            eventType = xpp.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType)
                {
                    case XmlPullParser.START_DOCUMENT:
                        //info = new ControlInfo();
                        break;
                    case XmlPullParser.START_TAG:
                        tagName = xpp.getName();
                        if( tagName.equals("pattern")) {
                            info  = new PatternInfo();
                        }else if(tagName.equals("lat")) {
                            isLat = true;
                        }else if(tagName.equals("lng")) {
                            isLng = true;
                        }else if(tagName.equals("time")) {
                            isTime = true;
                        }else if(tagName.equals("temp")) {
                            isTemp = true;
                        }else if(tagName.equals("set")) {
                            isSet = true;
                        }else if(tagName.equals("num")) {
                            isNum = true;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = xpp.getName();
                        if(tagName.equals("pattern"))
                        {
                            plist.add(info);
                        }else if(tagName.equals("lat")) {
                            isLat = false;
                        }else if(tagName.equals("lng")) {
                            isLng = false;
                        }else if(tagName.equals("time")) {
                            isTime = false;
                        }else if(tagName.equals("temp")) {
                            isTemp = false;
                        }else if(tagName.equals("set")) {
                            isSet = false;
                        }else if(tagName.equals("num")) {
                            isNum = false;
                        }
                    case XmlPullParser.TEXT:
                        if(isLat)
                        {
                            info.setLat(Double.valueOf(xpp.getText()));
                        }else if(isLng)
                        {
                            info.setLng(Double.valueOf(xpp.getText()));
                        }else if(isTime)
                        {
                            info.setTime(String.valueOf(xpp.getText()));
                        }else if(isTemp)
                        {
                            info.setTemp(Float.valueOf(xpp.getText()));
                        }else if(isSet)
                        {
                            info.setStatus(Integer.valueOf(xpp.getText()));
                        }else if(isNum)
                        {
                            info.setNum(Integer.valueOf(xpp.getText()));
                        }
                        break;
                }
                eventType = xpp.next();
            }

        } catch (Exception e)
        {

        }
    }


    public ControlInfo parseInfo(String result){

        boolean isName = false;
        boolean isStatus = false;
        boolean isDate = false;
        ControlInfo info = null;

        String tagName = null;

        try{
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result.trim()));
            eventType = xpp.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType)
                {
                    case XmlPullParser.START_DOCUMENT:
                        info = new ControlInfo();
                        break;
                    case XmlPullParser.START_TAG:
                        tagName = xpp.getName();
                        if( tagName.equals("sname"))
                        {
                            isName = true;
                        } else if(tagName.equals("sstatus")) {
                            isStatus = true;
                        }else if(tagName.equals("date")) {
                            isDate = true;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = xpp.getName();
                    case XmlPullParser.TEXT:
                        if(isName) {
                            info.setSname(xpp.getText());
                            isName = false;
                        }else if(isStatus) {
                            info.setSstatus(xpp.getText());
                            isStatus = false;
                        }else if(isDate) {
                            info.setSdate(xpp.getText());
                            isDate = false;
                        }
                        break;
                }
                eventType = xpp.next();
            }

        } catch (Exception e)
        {

        }
        return info;
    }

    ControlInfo getControlInfo()
    {
        return controlInfo;
    }

    class HttpThread extends Thread
    {
        //네트워크 작업을 할 thread
        int i = 0;
        @Override
        public void run() {
            super.run();
            try {
                String result = downloadUrl(getSiteURL());
                if(result.contains("Unregistered"))//등록되지 않은 기기
                {
                    handler.sendEmptyMessage(-1);
                    return;
                }

                if(result.contains("Failed"))//온도 요청 실패
                {
                    handler.sendEmptyMessage(-2);
                    return;
                }

                if(controlType == 4)
                {
                    parsePattern(result);

                    Log.d("and_result", result);
//                    Log.d("and_result_url",getSiteURL());
//                    Log.d("and_result_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));

                    if (handler != null)
                        handler.sendEmptyMessage(controlType);
                }else {
                    controlInfo = parseInfo(result);
                    Log.d("and_result", result);

                    if (handler != null)
                        handler.sendEmptyMessage(controlType);

                    if(controlType == 5) return;//온도는 한번만 가져오기 다른 기기는 delay를 고려해 한번 더 가져오는 것 수행

                    while(i < 2)
                    {
                        /* 데이터를 가져오는 평균 delay(1.6)초를 고려한 term 설정  */
                        try {
                            super.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        i++;
                    }

                    controlInfo = parseInfo(downloadUrl(getSiteURL()));
                    //Log.d("and_result", downloadUrl(getSiteURL()));

                    if (handler != null)
                        handler.sendEmptyMessage(controlType);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
