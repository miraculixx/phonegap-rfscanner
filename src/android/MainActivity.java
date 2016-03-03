/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.example.scandevice;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;


import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends CordovaActivity
{
    public DBHelper w_dbManager, b_dbManager, g_dbManager;
    private static Context context;

    private final int wifiDB = 1;
    private final int bteDB = 2;
    private final int gpsDB = 3;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();




        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }
    public void postJSONData(String url){
        new HttpAsyncTask().execute(url);
    }

    public static String excutePost(String targetURL, String urlParameters)
    {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("charset", "utf-8");


            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close();

        } catch (Exception e) {
            e.printStackTrace();
            return "Don't post data";

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
        return "Data Sent";
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            JSONObject jObject;
            JSONArray jArray_wifi, jArray_bte, jArray_gps;

            w_dbManager = DBHelper.getInstance(context, "WiFi.db", null, 1);
            jArray_wifi = w_dbManager.PrintData(wifiDB);

            b_dbManager = DBHelper.getInstance(context, "BTE.db", null, 1);
            jArray_bte = b_dbManager.PrintData(bteDB);

            g_dbManager = DBHelper.getInstance(context, "GPSList.db", null, 3);
            jArray_gps = g_dbManager.PrintData(gpsDB);


            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
            String date = sdf.format(new Date());

            jObject = new JSONObject();
            JSONObject dataObject = new JSONObject();

            try {
                dataObject.put("timestamp", date);
                dataObject.put("devices", jArray_bte);
                dataObject.put("networks", jArray_wifi);
                dataObject.put("gps", jArray_gps);

                jObject.put("choice", "scan");
                jObject.put("data", dataObject);
                jObject.put("comment", "rfscanner");
                jObject.put("poll", "/api/v1/polls/poll/rfscan");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return excutePost(urls[0], jObject.toString());
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            w_dbManager.delete("delete from SCAN_LIST where 1");
            b_dbManager.delete("delete from SCAN_LIST where 1");
            g_dbManager.delete("delete from SCAN_LIST where 1");
        }
    }
}
