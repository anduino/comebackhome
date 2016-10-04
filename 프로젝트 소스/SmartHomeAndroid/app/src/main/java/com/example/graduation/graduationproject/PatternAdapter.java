package com.example.graduation.graduationproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;

import java.util.ArrayList;

/**
 * Created by KIMYEACHAN on 2015-05-30.
 */
public class PatternAdapter extends CursorAdapter {
    Context context;
    Cursor m_cursor;
    ArrayList<Integer> userSelect;
    SQLiteDatabase db;
    String dbName = "anduino.db";
    String tableName = "patterns";

    //googlemap
    MapFragment mapFr;
    GoogleMap map;
    UiSettings uiSettings;

    public PatternAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.context = context;
        this.m_cursor = null;


    }

    public void setArrayList(ArrayList<Integer> array)
    {
        this.userSelect = array;
        this.changeCursor(m_cursor);
    }

    public void setDB(SQLiteDatabase db)
    {
        this.db = db;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.pattern_row, parent,false);
        return v;
    }

    @Override
    public void changeCursor(Cursor cursor) { // When user searching
        super.changeCursor(cursor);
        if(cursor != null)
        {
            this.m_cursor = cursor;
            this.m_cursor.moveToFirst();
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(m_cursor != null) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.pattern_row, null);
            }

            final TextView tempText = (TextView) v.findViewById(R.id.tempText);
            final Switch swit = (Switch) v.findViewById(R.id.pbutton);
            final TextView timeText = (TextView) v.findViewById(R.id.timeText);
            //final Button btn = (Button) v.findViewById(R.id.mapBtn);

            m_cursor.moveToPosition(position);

            // db 내용 ui 내용으로 converting
            float tempValue = m_cursor.getFloat(m_cursor.getColumnIndex("temp"));
            double value = Math.round(tempValue*10)/10.0;


            // 0 -> off 1 -> on
            String statusValue = String.valueOf(m_cursor.getInt(m_cursor.getColumnIndex("status")));

            // 30분 단위로 나눈 시간.
            int timeValue = m_cursor.getInt(m_cursor.getColumnIndex("time"));
            String hour = String.valueOf(timeValue/2);
            String min = String.valueOf((timeValue * 30)%60);

            //위치
            final double lat = m_cursor.getDouble(m_cursor.getColumnIndex("lat"));
            final double lng = m_cursor.getDouble(m_cursor.getColumnIndex("lng"));

            final int num = m_cursor.getInt(m_cursor.getColumnIndex("num"));

            tempText.setText(String.valueOf(value)+context.getResources().getString(R.string.cel));
            timeText.setText(hour + " : " + min);

            if(statusValue.contains("0"))// off
            {
                swit.setChecked(false);
            }else{ // on
                swit.setChecked(true);
            }

            swit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String sql = null;
                    if(isChecked) {
                        sql = "update " + tableName + " set status =" + String.valueOf(1) + " where num =" + num + "; ";
                        db.execSQL(sql);

                        Intent i = new Intent(context.getApplicationContext(), PatternService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);;
                        context.stopService(i);
                        context.startService(i);
                    } else {
                        sql = "update " + tableName + " set status ="
                                + String.valueOf(0) + " where num =" + num + "; ";
                        db.execSQL(sql);
                    }
                }
            });

//            btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent i = new Intent(context.getApplicationContext(), MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);;
//                    i.putExtra("lat",lat);
//                    i.putExtra("lng",lng);
//                    context.startActivity(i);
//                }
//            });

            return v;
        }else {
            return null;
        }
    }


    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
    }
}
