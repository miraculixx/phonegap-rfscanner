package com.example.scandevice;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Slonic on 1/28/2016.
 */
public class JSONCollection extends Activity {


    public static JSONArray m_jArray_net;
    public static JSONArray m_jArray_bte;
    public static int checkboth = 0;
    JSONObject jObject;

    public JSONCollection(JSONArray jArray, int v){

        if (v == 1){//networks
            m_jArray_net = jArray;
            checkboth++;
        }
        if (v == 2){//bte
            m_jArray_bte = jArray;
            checkboth++;
        }
        if (checkboth == 2){
            PostJSONServer();
        }
    }

    private void PostJSONServer(){

//        LocationManager mlocManager=null;
//        LocationListener mlocListener;
//
//        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        mlocListener = new MyLocationListener();


        checkboth = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
        String date=sdf.format (new Date() );

        jObject = new JSONObject();
        try {
            jObject.put("timestamp", date);
            jObject.put("devices", m_jArray_bte);
            jObject.put("networks", m_jArray_net);

            postData("https://cpdev.dockrzone.com/api/v1/polls/vote/", jObject);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
            String temp = EntityUtils.toString(response.getEntity());
            Log.e("tag", json);
            Log.e("tag", temp);


        } catch (ClientProtocolException e) {
            e.printStackTrace();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
