package com.cyclist.UI;

import android.content.Context;

import com.cyclist.logic.LogicManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class UIManager implements OnCenterMeClick, OnLocationChanged {


    private Context mContext;
    private MapView map;
    private GeoPoint currentDeviceLocation;
    private MyLocationNewOverlay myLocationOverlay;
    private CompassOverlay compassOverlay;
    private RotationGestureOverlay rotationGestureOverlay;

    public UIManager(Context mContext) {
        this.mContext = mContext;
    }


    public void setMap(MapView map) {
        this.map = map;
        initMap();
    }

    private void initMap(){
        map.setTileSource(TileSourceFactory.HIKEBIKEMAP);
        //map.setBuiltInZoomControls(true);
        map.setTilesScaledToDpi(true);
        map.setFlingEnabled(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setCenter(LogicManager.getInstance().getCurrentLocation());
        mapController.setZoom(19d);


        setMapCompass();
        setMyLocation();
        this.rotationGestureOverlay = new RotationGestureOverlay(map);
        rotationGestureOverlay.setEnabled(true);

        map.getOverlays().add(this.myLocationOverlay);
        map.getOverlays().add(this.compassOverlay);
        map.getOverlays().add(this.rotationGestureOverlay);
    }

    private void setMyLocation() {
        this.myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext),
                map);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.setOptionsMenuEnabled(true);
    }

    private void setMapCompass() {
        this.compassOverlay = new CustomCompassOverlay(mContext, new InternalCompassOrientationProvider(mContext),
                map);
        compassOverlay.enableCompass();
    }

    @Override
    public void handleCenterMapClick() {
        if (currentDeviceLocation != null) {
            map.getController().setZoom(18d);
            map.getController().animateTo(currentDeviceLocation);
            map.setMapOrientation(0);
        }
    }


    @Override
    public void onLocationChanged(GeoPoint location) {
        currentDeviceLocation = location;
        map.getController().animateTo(currentDeviceLocation);
        // TODO:: Remove old markers and Add new marker to location.

    }
}
