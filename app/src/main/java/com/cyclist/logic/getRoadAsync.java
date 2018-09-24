package com.cyclist.logic;

import android.os.AsyncTask;

import com.cyclist.UI.OnRoadCalculated;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

/**
 * Created by LironSeliktar on 9/15/18.
 */

public class getRoadAsync extends AsyncTask<RoutePoints, Void, Road> {
    private OnRoadCalculated presenter;
    private Integer color;
    private LogicManager logicManager;

    public getRoadAsync(OnRoadCalculated presenter, Integer color) {
        this.presenter = presenter;
        this.color = color;
    }

    @Override
    protected Road doInBackground(RoutePoints... routePoints) {
        ArrayList<GeoPoint> points = routePoints[0].getData();
        logicManager = LogicManager.getInstance();
        RoadManager roadManager = logicManager.getRoadManger();
        return roadManager.getRoad(points);
    }


    @Override
    protected void onPostExecute(Road road) {
        if (road == null)
            return;
        if (road.mStatus == Road.STATUS_TECHNICAL_ISSUE) {
            presenter.showErrorMsg("Technical issue when getting the route");
            return;
        }
        else if (road.mStatus > Road.STATUS_TECHNICAL_ISSUE) {//functional issues
            presenter.showErrorMsg("No possible route here");
            return;
        }

        logicManager.setPresentedRoad(road);
        Polyline roadOverlay;
        if (color != null) {
            roadOverlay = RoadManager.buildRoadOverlay(road, color, 12.0f);
        } else {
            roadOverlay = RoadManager.buildRoadOverlay(road);
        }
        presenter.promptRouteDetails(road, roadOverlay);
    }
}
