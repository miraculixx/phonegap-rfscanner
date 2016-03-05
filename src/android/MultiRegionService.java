package com.example.scandevice;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MultiRegionService extends Service implements ConnectionCallbacks, LocationListener, OnConnectionFailedListener, ResultCallback<Status> {

    public static final String COORD = "coord arry";
    public String Coord;

    protected static final String TAG = "MultiRegionService";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The list of geofences used in this sample.
     */
    protected ArrayList<Geofence> mGeofenceList;

    /**
     * Used to keep track of whether geofences were added.
     */
    private boolean mGeofencesAdded;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    /**
     * Used to persist application state about whether geofences were added.
     */
    private SharedPreferences mSharedPreferences;

    public static final String PACKAGE_NAME = "com.example.scandevice";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;


    private Handler mHandler;
    private HandlerThread handlerThread;
    public static final String TIME_STAMP = "10000";
    private int mTimeInterval = 0, count = 0, mCount = 0;;
    private final static int mMinute = 60000;
    public  GeofenceReceiver subGeofenceReceiver;
    boolean mBound = false;

    private double lat, lon, alt;
    private String timestamp = "yyyyMMdd'T'HH:mm:ss";

    String provider;
    public DBHelper GPS_dbManager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Coord = intent.getStringExtra(COORD);
        mTimeInterval = Integer.parseInt(intent.getStringExtra(TIME_STAMP));
        count = mMinute/Integer.parseInt(intent.getStringExtra(TIME_STAMP));
        Log.w(TAG, Coord);

        mGeofenceList = new ArrayList<Geofence>();
        mGeofencePendingIntent = null;
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        mGeofencesAdded = mSharedPreferences.getBoolean(GEOFENCES_ADDED_KEY, false);
        populateGeofenceRegion(Coord);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        addGeofences();

        handlerThread = new HandlerThread("StartGeofenceReceiver");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        mHandler.post(mRunnable);


        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int cause) {  Log.i(TAG, "Connection suspended");  }

    @Override
    public void onConnectionFailed(ConnectionResult result) {  Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode()); }

    public void onResult(Status status) {
        if (status.isSuccess()) {
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void populateGeofenceList(String identifier, float lat, float lon, int radius){
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(identifier)
                    .setCircularRegion(lat, lon, radius)
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

    }

    private void populateGeofenceRegion(String coord){
        String identifer = "", lat = "", lon = "", radius = "";
        String[] countRegion = coord.split(";");
        String[] coordValue, regionValue;
        int jk = 0;

        for (int i = 0; i < countRegion.length; i++){
            coordValue = countRegion[i].split(",");
            for (int j = 0; j < coordValue.length; j++){
                regionValue = coordValue[j].split(":");
                for (int k = 0; k < regionValue.length; k++){
                    if (k == 1) {
                        switch (jk) {
                            case 0: identifer = regionValue[k]; jk++;   break;
                            case 1: lat = regionValue[k];       jk++;   break;
                            case 2: lon = regionValue[k];       jk++;   break;
                            case 3: radius = regionValue[k];    jk = 0; break;
                        }
                    }
                }
            }
            populateGeofenceList(identifer, Float.parseFloat(lat), Float.parseFloat(lon), Integer.parseInt(radius));
        }
    }

    public void addGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG, "isConnected "+getString(R.string.not_connected));
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceReceiver.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
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
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}

    private void setLocationSetting(){
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


        if(location != null)
            onLocationChanged(location);
    }



    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            GeofenceReceiver.GeofenceReceiverHandle binder = (GeofenceReceiver.GeofenceReceiverHandle)iBinder;
            subGeofenceReceiver = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mHandler != null) {
                if (mCount == count) {
                    stop(); return;
                }
                ContentValues getGeoValue = subGeofenceReceiver.getGeofenceType();
                SQLinsert(getGeoValue.get("geoID").toString(), getGeoValue.get("geoType").toString());
                mHandler.postDelayed(mRunnable, mTimeInterval);
                mCount++;
            }
        }
    };
    private void SQLinsert(String geoID, String geoType){
        setLocationSetting();
        GPS_dbManager = new DBHelper(getApplicationContext(), "GPSList.db", null, 3);
        GPS_dbManager.insert("insert into SCAN_LIST ('_id', 'identifier', 'enter', 'lat', 'lon', 'lat', 'timestamp') values(null,'" +
                geoID + "', '" + geoType+ "', '" + lat + "', '" + lon + "', '" + alt + "', '" + timestamp + "');");
    }
    private void stop(){
        mCount = 0;
        mHandler.removeCallbacks(mRunnable);
        handlerThread.quit();
        handlerThread.interrupt();
        handlerThread = null;
        mHandler = null;
        unbindService(mConnection);
        mBound = false;
        Thread.interrupted();
    }
}
