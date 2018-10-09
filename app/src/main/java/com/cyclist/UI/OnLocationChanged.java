package com.cyclist.UI;


import android.support.v4.util.Pair;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;


public interface OnLocationChanged {

    void onLocationChanged(GeoPoint location);
    void showNewInstruction(Pair<Integer, String> pair);
    MapView getMapView();
    void showRouteDetailsBar(Double length, Double duration);
}
