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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends CordovaActivity implements LocationListener
{
    public DBHelper w_dbManager, b_dbManager;
    private static Context context;

    private final int wifiDB = 1;
    private final int bteDB = 2;

    private static double lat, lon, alt;
    private static String timestamp = "yyyyMMdd'T'HH:mm:ss";

    String provider;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, true);


        if(provider == null && !locationManager.isProviderEnabled(provider)){

            // Get the location from the given provider
            List<String> list = locationManager.getAllProviders();

            for(int i = 0; i < list.size(); i++){
                //Get device name;
                String temp = list.get(i);

                //check usable
                if(locationManager.isProviderEnabled(temp)){
                    provider = temp;
                    break;
                }
            }
        }
        //get location where reference last.
        Location location = locationManager.getLastKnownLocation(provider);


        if(location == null){
            Toast.makeText(this, "There are no available position information providers.", Toast.LENGTH_SHORT).show();
            //onLocationChanged(location);
        }else{
            //GPS start from last location.
            onLocationChanged(location);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }
    public void PostJSONData(String url){

        JSONObject jObject;
        JSONArray jArray_wifi, jArray_bte;

        w_dbManager = new DBHelper(context, "WiFi.db", null, 1);
        jArray_wifi = w_dbManager.PrintData(wifiDB);

        b_dbManager = new DBHelper(context, "BTE.db", null, 1);
        jArray_bte = b_dbManager.PrintData(bteDB);


        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
        String date=sdf.format (new Date());

        jObject = new JSONObject();
        JSONObject subObject = new JSONObject();

        try {
            subObject.put("lat", lat);
            subObject.put("lon", lon);
            subObject.put("alt", alt);
            subObject.put("timestamp", timestamp);

            jObject.put("choice","scan");
            jObject.put("timestamp", date);
            jObject.put("devices", jArray_bte);
            jObject.put("networks", jArray_wifi);
            jObject.put("gps", subObject);
            jObject.put("comment", "rfscanner");
            jObject.put("poll", "/api/v1/polls/poll/rfscan");


            Log.w("tag", "Posting...");
            postData(url, jObject);
            //postData("https://cpdev.dockrzone.com/api/v1/polls/vote/", jObject);
            Log.w("tag", "Posting end....");

            Log.w("tag", "DB deleting...");
            w_dbManager.delete("delete from SCAN_LIST where 1");
            b_dbManager.delete("delete from SCAN_LIST where 1");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }
    @Override
    public void onLocationChanged(Location location) {

        lat = location.getLatitude();
        lon = location.getLongitude();
        alt = location.getAltitude();

        SimpleDateFormat GPStime = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
        timestamp = GPStime.format (location.getTime());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
    }

    public void postData(String url,JSONObject obj) {
        // Create a new HttpClient and Post Header

        HttpParams myParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(myParams, 10000);
        HttpConnectionParams.setSoTimeout(myParams, 10000);
        HttpClient httpclient = new DefaultHttpClient(myParams );
        String json=obj.toString();

        try {

            HttpPost httppost = new HttpPost(url.toString());
            httppost.setHeader("Content-type", "application/json");

            StringEntity se = new StringEntity(obj.toString());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httppost.setEntity(se);

            HttpResponse response = httpclient.execute(httppost);
            Log.e("tag", json);

        } catch (ClientProtocolException e) {
            e.printStackTrace();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}