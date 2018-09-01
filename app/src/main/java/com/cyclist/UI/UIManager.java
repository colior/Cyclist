package com.cyclist.UI;

import android.location.Location;

import com.cyclist.Logic.LogicManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class UIManager {

    private MapView map;
    private Location currentDeviceLocation;


    public void setMap(MapView map) {
        this.map = map;
        initMap();
    }
    private void initMap(){
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setCenter(LogicManager.getInstance().getCurrentLoction());
        mapController.setZoom(9d);
    }
}
