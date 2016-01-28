   package com.example.scandevice;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.ComponentName;
    /**
     * This class echoes a string called from JavaScript.
     */
    public class ScanService extends CordovaPlugin {
        Intent WiFiIntent;
        Intent BTEIntent;
        private DBHelper dbHelper;
        private static JSONObject defaultSettings = new JSONObject();
        private static JSONObject updateSettings;
        public static String www="www";
        private enum Event {
            ACTIVATE, DEACTIVATE, FAILURE
        }
        private final ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                // Nothing to do here
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // Nothing to do here
            }
        };

        @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
            Log.i("service","contected");
           // setDefaultSettings(args.getJSONObject(0));
             if (action.equals("start")) {
                Activity context = cordova.getActivity();
               //  dbHelper = new DBHelper(context,"WiFi.db", null, 1);
                WiFiIntent = new Intent(context, WifiScanService.class);
                WiFiIntent.putExtra(WifiScanService.TIME_STAMP, "1000");
                try {
                    context.bindService(WiFiIntent, connection, Context.BIND_AUTO_CREATE);
                    context.startService(WiFiIntent);
                } catch (Exception e) {
                    Log.i("Failure", e.getMessage());
                }
                try{
                    Thread.sleep(3000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
              // String str= dbHelper.PrintData();
                //activity.startService(WiFiIntent);
                callbackContext.success(this.www);
                String message = args.getString(0);
                return true;
            } else if(action.equals("stop")){
                Activity context = cordova.getActivity();

                Intent intent = new Intent(
                        context, WifiScanService.class);
                // String str= dbHelper.PrintData();
                context.unbindService(connection);
                context.stopService(intent);
                callbackContext.success(www);
                return true;
            }
            return false;
        }

        private void echo(String message, CallbackContext callbackContext) {
            Log.i("service", "echo");
            if (message != null && message.length() > 0) {
                callbackContext.success(message);
            } else {
                callbackContext.error("Expected one non-empty string argument.");
            }
        }


    }