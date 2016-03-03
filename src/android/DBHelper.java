package com.example.scandevice;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DBHelper extends SQLiteOpenHelper {

    private int g_version;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        g_version = version;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w("Version", "SQL = " + g_version);
        if (g_version == 3)
            db.execSQL("CREATE TABLE SCAN_LIST( _id INTEGER PRIMARY KEY AUTOINCREMENT, identifier TEXT, enter TEXT, lat TEXT, lon TEXT, alt TEXT, timestamp TEXT);");
        else
            db.execSQL("CREATE TABLE SCAN_LIST( _id INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, SIGNAL INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void delete(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public JSONArray PrintData(int v) {
        SQLiteDatabase db = getReadableDatabase();
        JSONArray jArray = new JSONArray();

        Cursor cursor = db.rawQuery("select * from SCAN_LIST", null);
        while(cursor.moveToNext()) {

            if(v == 1) {// networks:
                JSONObject subObject = new JSONObject();
                try {
                    subObject.put("ssid", cursor.getString(1));
                    subObject.put("signal", cursor.getInt(2));
                    jArray.put(subObject);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            else if (v == 2){//BTE:

                JSONObject subObject = new JSONObject();
                try {
                    subObject.put("mac", cursor.getString(1));
                    subObject.put("signal", cursor.getInt(2));
                    jArray.put(subObject);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else if (v == 3){//GPS:
                JSONObject subObject = new JSONObject();
                JSONObject regionObject = new JSONObject();
                JSONObject gpsObject = new JSONObject();

                Calendar c = Calendar.getInstance();
                SimpleDateFormat strtime = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
                String timestamp = strtime.format(c.getTime());
                try {
                    regionObject.put(cursor.getString(1), cursor.getString(2));

                    gpsObject.put("lat", cursor.getString(3));
                    gpsObject.put("lon", cursor.getString(4));
                    gpsObject.put("alt", cursor.getString(5));
                    gpsObject.put("timestamp", cursor.getString(6));
                                                            //    {
                    subObject.put("timestamp", timestamp);  //      timestamp: 'yyyymmddThhmmss+000' ,
                    subObject.put("source", "android");     //      source: “android”,
                    subObject.put("gps", gpsObject);        //      gps: { lat: , lon:, alt: , timestamp: yyymmddThhmmss+000 } // for location monitoring
                    subObject.put("region", regionObject);  //      region: { “identifier” : “enter | exit” } // for region monitoring
                                                            //    },
                    jArray.put(subObject);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return jArray;
    }
}