package com.cyclist.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cyclist.UI.OnLocationChanged;

import org.osmdroid.util.GeoPoint;

import lombok.Getter;

import static com.cyclist.logic.common.Constants.BROADCAST_ACTION;
import static com.cyclist.logic.common.Constants.LAT_TAG;
import static com.cyclist.logic.common.Constants.LONG_TAG;

public class LocationReceiver extends BroadcastReceiver {
    @Getter
    private GeoPoint currentLocation;
    private Context mContext;
    private LogicManager logicManager;
    private OnLocationChanged listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (logicManager == null) {
            logicManager = logicManager.getInstance();
            listener = logicManager.getOnLocationChangedListener();
        }
        if (intent.getAction().equals(BROADCAST_ACTION)) {
            if (mContext == null) {
                mContext = context;
            }
            Double latDefualt, longDefualt;

            if (currentLocation != null) {
                latDefualt = currentLocation.getLatitude();
                longDefualt = currentLocation.getLongitude();
            } else {
                latDefualt = 32.070120d;
                longDefualt = 34.771720d;
                //TODO:: Change these points which points to my house.
            }

            Double latitude = intent.getDoubleExtra(LAT_TAG, latDefualt);
            Double longitude = intent.getDoubleExtra(LONG_TAG, longDefualt);
            currentLocation = new GeoPoint(latitude, longitude);
            if (listener != null) {
                listener.onLocationChanged(currentLocation);
                logicManager.setCurrentLocation(currentLocation);
            }
        }
    }
}
