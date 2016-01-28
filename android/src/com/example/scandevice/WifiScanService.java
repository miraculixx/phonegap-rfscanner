package com.example.scandevice;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by Slonic on 1/27/2016.
 */
public class WifiScanService extends Service implements Runnable {


    private Handler mHandler;
    private static final int TIMER_PERIOD = 1 * 1000;
    public static final String TIME_STAMP = "10000";
    public static final String STOP_FLAG = "FALSE";
    public static boolean endServiceFlag = false;
    public static int curCount = 0;
    public Intent mIntent;

    private long preTime;
    private long curTime;
    private int TimeStamp;
    private int count;
    public DBHelper dbManager;
    public static final String EXTRA_KEY_IN = "EXTRA_IN";

    WifiManager mainWifi;
    WifiReceiver receiverWifi;

    @Override
    public void onCreate(){
        mHandler = new Handler();
        dbManager = new DBHelper(getApplicationContext(), "WiFi.db", null, 1);
        dbManager.delete("delete from SCAN_LIST where 1");
//        JSONArray jArray = dbManager.PrintData(1);
//        new JSONCollection(jArray, 1);
        Log.w("WIFI", "********** WiFi Scanning........");

    }
    @Override
    public boolean stopService(Intent name) {
        // TODO Auto-generated method stub
        JSONArray jArray = dbManager.PrintData(1);
        new JSONCollection(jArray, 1);
        endServiceFlag = false;
        curCount = 0;
        return super.stopService(name);

    }

    @Override
    public void onStart(Intent intent, int startId){
        mIntent = intent;
        CheckWiFiState();
        TimeStamp = Integer.parseInt(mIntent.getStringExtra(TIME_STAMP));
        count = 60000/TimeStamp;
        preTime = System.currentTimeMillis();// - DAY_TIME;
        mHandler.postDelayed(this, 1000);
    }
    private void CheckWiFiState(){
        WifiManager wifi =(WifiManager)getSystemService(Context.WIFI_SERVICE);
        if(wifi.isWifiEnabled())
            wifi.setWifiEnabled(true);
        curCount = 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    /*
     */
    @Override
    public void run() {
        curTime = System.currentTimeMillis();
        Log.d("curTime]",""+curTime);
        Log.d("PreTime]",""+preTime);

        long CUR_PERIOD = curTime - preTime;
        if( CUR_PERIOD >= TimeStamp ){
            mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            receiverWifi = new WifiReceiver();
            registerReceiver(receiverWifi, new IntentFilter(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            mainWifi.startScan();


            preTime = curTime;
            curCount++;

            if(curCount == count)
                endServiceFlag = true;
        }

        if(endServiceFlag){
            stopService(mIntent);
        }else{
            mHandler.postDelayed(this, TIMER_PERIOD);
        }
    } //run end!!!


    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            int maxLevel = 5;
            List<ScanResult> wifiList;
            wifiList = mainWifi.getScanResults();
            for (ScanResult result : wifiList) {
                {
                    int level = WifiManager.calculateSignalLevel(result.level, maxLevel);
                    String SSID = result.SSID;
                    String capabilities = result.capabilities;
                    dbManager.insert("insert into SCAN_LIST values(null,'" + SSID + "', '" + level + "');");
                    Log.w("WIFI", "SSID:" + SSID + "\nCapabilities:" + capabilities + "\nSginal Strength:" + level);
                }
            }
        }
    }

}// class end