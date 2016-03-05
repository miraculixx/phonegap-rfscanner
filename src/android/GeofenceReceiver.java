package com.example.scandevice;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


public class GeofenceReceiver extends IntentService {

	String transitionName;
	String geofenceID;
	private final IBinder mBinder = new GeofenceReceiverHandle();
	public Geofence geofence;

	public GeofenceReceiver() {
		super("GeofenceReceiver");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GeofencingEvent geoEvent = GeofencingEvent.fromIntent(intent);
		if (geoEvent.hasError()) {
			Log.d(MainActivity.TAG, "Error GeofenceReceiver.onHandleIntent");
		} else {
			Log.d(MainActivity.TAG, "GeofenceReceiver : Transition -> "
					+ geoEvent.getGeofenceTransition());

			int transitionType = geoEvent.getGeofenceTransition();

			if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
					|| transitionType == Geofence.GEOFENCE_TRANSITION_DWELL
					|| transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
				List<Geofence> triggerList = geoEvent.getTriggeringGeofences();

				for (Geofence geofence : triggerList) {
					transitionName = "";
					geofenceID = "";
					switch (transitionType) {
					case Geofence.GEOFENCE_TRANSITION_DWELL:
						transitionName = "dwell";
						geofenceID = geofence.getRequestId();
						break;

					case Geofence.GEOFENCE_TRANSITION_ENTER:
						transitionName = "Enter";
						geofenceID = geofence.getRequestId();
						break;

					case Geofence.GEOFENCE_TRANSITION_EXIT:
						transitionName = "Exit";
						geofenceID = geofence.getRequestId();
						break;
					}
				}
			}
		}
	}
	public class GeofenceReceiverHandle extends Binder {
		GeofenceReceiver getService(){
			return GeofenceReceiver.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent){
		return mBinder;
	}
	public synchronized ContentValues getGeofenceType(){
		ContentValues values = new ContentValues();
		values.put("geoType", transitionName);
		values.put("geoID", geofenceID);
		return values;
	}
}
