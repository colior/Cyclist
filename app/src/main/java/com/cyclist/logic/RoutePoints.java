package com.cyclist.logic;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * Created by LironSeliktar on 9/15/18.
 */

public class RoutePoints {
    private ArrayList<GeoPoint> points;

    public RoutePoints(ArrayList<GeoPoint> points) {
        this.points = points;
    }

    public ArrayList<GeoPoint> getData() {
        return points;
    }
}