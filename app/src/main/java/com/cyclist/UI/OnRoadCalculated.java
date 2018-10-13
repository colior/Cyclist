package com.cyclist.UI;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.views.overlay.Polyline;

public interface OnRoadCalculated {
    void showErrorMsg(String msg);
    void promptRouteDetails(Road road, Polyline overlay);
}
