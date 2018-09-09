package com.cyclist.UI;

import android.content.Context;
import android.view.MotionEvent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.IOrientationProvider;

import static android.view.MotionEvent.ACTION_DOWN;

/**
 * Created by LironSeliktar on 9/8/18.
 */

public class CustomCompassOverlay extends CompassOverlay {
    public CustomCompassOverlay(Context context, MapView mapView) {
        super(context, mapView);
    }

    public CustomCompassOverlay(Context context, IOrientationProvider orientationProvider, MapView mapView) {
        super(context, orientationProvider, mapView);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
        switch (e.getAction()) {
            case ACTION_DOWN:
                mapView.setMapOrientation(0);
                break;
        }
        return true;
    }

}
