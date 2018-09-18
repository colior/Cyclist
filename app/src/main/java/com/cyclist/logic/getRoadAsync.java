package com.cyclist.logic;

import android.os.AsyncTask;

import com.cyclist.UI.UIManager;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

/**
 * Created by LironSeliktar on 9/15/18.
 */

public class getRoadAsync extends AsyncTask<RoutePoints, Void, Road> {
    private UIManager uiManager;
    private Integer color;

    public getRoadAsync(UIManager uiManager, Integer color) {
        this.uiManager = uiManager;
        this.color = color;
    }

    @Override
    protected Road doInBackground(RoutePoints... routePoints) {
        ArrayList<GeoPoint> points = routePoints[0].getData();
        RoadManager roadManager = LogicManager.getInstance().getRoadManger();
        return roadManager.getRoad(points);
    }


    @Override
    protected void onPostExecute(Road road) {
        if (road == null)
            return;
        if (road.mStatus == Road.STATUS_TECHNICAL_ISSUE)
            uiManager.showErrorMsg("Technical issue when getting the route");
        else if (road.mStatus > Road.STATUS_TECHNICAL_ISSUE) //functional issues
            uiManager.showErrorMsg("No possible route here");

        Polyline roadOverlay;
        if (color != null) {
            roadOverlay = RoadManager.buildRoadOverlay(road, color, 12.0f);
        } else {
            roadOverlay = RoadManager.buildRoadOverlay(road);
        }

        uiManager.drawRoute(roadOverlay);
    }
}
