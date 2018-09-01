package com.cyclist.Logic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.cyclist.Logic.Common.Constants;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import static android.content.Context.LOCATION_SERVICE;
import static com.cyclist.Logic.Common.Constants.LOCATION_REFRESH_DISTANCE;
import static com.cyclist.Logic.Common.Constants.LOCATION_REFRESH_TIME;

public class LogicManager {
    private static LogicManager ourInstance = null;
    private Activity mActivity;
    private Context mContext;

    private RoadManager roadManager = new MapQuestRoadManager(Constants.MAPQUEST_KEY);
    //roadManager.addRequestOption("routeType=bicycle");
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private IGeoPoint currentLoction;

    private LogicManager() {

    }

    public static LogicManager getInstance() {
        if (ourInstance == null) {
            synchronized (LogicManager.class) {
                if (ourInstance == null) {
                    ourInstance = new LogicManager();
                    return ourInstance;
                }
            }
        }
        return ourInstance;
    }

    public void setUpLocationListner(Activity activity) {
        mContext = mActivity = activity;
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLoction = new GeoPoint(location);
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
        };
        mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
        } else {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Ask for perrmission
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
            }
        }
    }

    public IGeoPoint getCurrentLoction() {
        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, false);
        if (currentLoction == null) {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            return new GeoPoint(mLocationManager.getLastKnownLocation(bestProvider));
        }
        return currentLoction;
    }
}

