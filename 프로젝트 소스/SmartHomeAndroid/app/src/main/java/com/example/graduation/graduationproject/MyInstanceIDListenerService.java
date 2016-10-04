package com.example.graduation.graduationproject;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Jihae on 2016-05-14.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService{
    private static final String TAG = "MyInstanceIDLS";

    @Override
    public void onTokenRefresh() {
        //Instance 토큰이 바뀔 때 이 함수가 호출되는듯
        //preference바꿔주고..서버에도 보내주고..

        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}
