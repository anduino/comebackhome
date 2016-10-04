package com.example.graduation.graduationproject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


public class SettingActivity extends Activity {

    String dbName = "anduino.db";
    String tableNamePattern = "patterns";
    String tableNameUser = "user";
    SQLiteDatabase db;
    TextView userNameView;
    String userName;
    Switch gcmSwitch;

    private SharedPreferences initSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        initSettingActivity();
    }

    private void initSettingActivity() {

        initSetting = getSharedPreferences("initSetting", 0);

        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE,null);
        userNameView = (TextView)findViewById(R.id.setting_name);

        Cursor cursor = db.rawQuery("select name from user;", null);
        cursor.moveToFirst();
        userName = cursor.getString(cursor.getColumnIndex("name"));

        userNameView.setText(userName);

        gcmSwitch = (Switch)findViewById(R.id.gcmSwitch);

        gcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    SharedPreferences.Editor editor;
                    editor = initSetting.edit();

                    editor.putBoolean("gcmOn", true);
                    editor.commit();
                }else{
                    SharedPreferences.Editor editor;
                    editor = initSetting.edit();

                    editor.putBoolean("gcmOn", false);
                    editor.commit();
                }
            }
        });

        gcmSwitch.setChecked(initSetting.getBoolean("gcmOn", true));

    }

    public void setBtnClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.backBtn:
                finish();
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
