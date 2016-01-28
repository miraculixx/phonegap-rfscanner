package com.example.scandevice;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONArray;

/**
 * Created by Slonic on 1/27/2016.
 */
public class BluetoothScanService extends Service implements Runnable {


    private Handler mHandler;
    private static final int TIMER_PERIOD = 1 * 1000;
    public static final String TIME_STAMP = "10000";

    private long preTime;
    private long curTime;
    private int TimeStamp;
    private int count;
    public Intent mIntent;

    BluetoothAdapter bluetoothAdapter;
    private static int REQUEST_BLUETOOTH = 1;
    public static boolean endServiceFlag = false;
    public static int curCount = 0;
    public DBHelper dbManager;


    @Override
    public void onCreate(){
        mHandler = new Handler();
        CheckBlueToothState();
        dbManager = new DBHelper(getApplicationContext(), "BTE.db", null, 1);
        dbManager.delete("delete from SCAN_LIST where 1");
//        JSONArray jArray = dbManager.PrintData(2);
//        new JSONCollection(jArray, 2);
        Log.w("BTE", "********** BTE Scanning........");
    }
    @Override
    public boolean stopService(Intent name) {
        // TODO Auto-generated method stub
        JSONArray jArray = dbManager.PrintData(2);
        new JSONCollection(jArray, 2);
        endServiceFlag = false;
        curCount = 0;
        return super.stopService(name);
    }

    @Override
    public void onStart(Intent intent, int startId){
        mIntent = intent;
        TimeStamp = Integer.parseInt(mIntent.getStringExtra(TIME_STAMP));
        count = 60000/TimeStamp;
        preTime = System.currentTimeMillis();// - DAY_TIME;
        curCount = 0;
        mHandler.postDelayed(this, 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void CheckBlueToothState(){
        if (bluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), "Bluetooth NOT support", Toast.LENGTH_LONG).show();
        }else{
            if (bluetoothAdapter.isEnabled()){
                if(bluetoothAdapter.isDiscovering()){
                    //Toast.makeText(getApplicationContext(), "Bluetooth is currently in device discovery process.", Toast.LENGTH_LONG).show();
                }else{
                    //Toast.makeText(getApplicationContext(), "Bluetooth is Enabled.", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Bluetooth is NOT Enabled!", Toast.LENGTH_LONG).show();
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        }
    }

    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.w("BLE", "Bluetooth Address: " + device.getAddress() + "\nBluetooth Signal: " + rssi);
                dbManager.insert("insert into SCAN_LIST values(null,'" + device + "', '" + rssi + "');");
            }
        }};

    /*
     */
    @Override
    public void run() {
        curTime = System.currentTimeMillis();
        Log.w("curTime]",""+curTime);
        Log.w("PreTime]",""+preTime);



        long CUR_PERIOD = curTime - preTime;
        if( CUR_PERIOD > TimeStamp ){
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            registerReceiver(ActionFoundReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_FOUND));
            bluetoothAdapter.startDiscovery();

            preTime = curTime;
            curCount++;
            if(curCount == count) {
                endServiceFlag = true;
            }
        }
        if(endServiceFlag){
            stopService(mIntent);
            //return;
        }else{
            mHandler.postDelayed(this, TIMER_PERIOD);
        }
    } //run end!!!
}// class end



