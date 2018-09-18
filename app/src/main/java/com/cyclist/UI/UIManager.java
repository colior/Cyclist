package com.cyclist.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.ThunderforestTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import static org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK;

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
        if (ThunderforestTileSource.haveMapId(mContext)) {
            ThunderforestTileSource tileSource = new ThunderforestTileSource(mContext, ThunderforestTileSource.CYCLE);
            TileSourceFactory.addTileSource(tileSource);
            map.setTileSource(TileSourceFactory.getTileSource(ThunderforestTileSource.mapName(ThunderforestTileSource.CYCLE)));
        } else {
            map.setTileSource(MAPNIK);
        }
        initMap();
    }

    private void initMap() {
        map.setTilesScaledToDpi(true);
        map.setFlingEnabled(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setCenter(LogicManager.getInstance().getCurrentLocation());
        mapController.setZoom(19d);


        setMapCompass();
        initMyLocation();
        this.rotationGestureOverlay = new RotationGestureOverlay(map);
        rotationGestureOverlay.setEnabled(true);

        map.getOverlays().add(this.myLocationOverlay);
        map.getOverlays().add(this.compassOverlay);
        map.getOverlays().add(this.rotationGestureOverlay);
    }

    public void pauseFollowMe() {
        this.myLocationOverlay.disableFollowLocation();
        this.myLocationOverlay.disableMyLocation();
    }

    public void resumeFollowMe() {
        this.myLocationOverlay.enableFollowLocation();
        this.myLocationOverlay.enableMyLocation();
    }

    private void initMyLocation() {
        this.myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext),
                map);
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.setOptionsMenuEnabled(true);
        myLocationOverlay.setPersonIcon(drawableToBitmap(mContext.getResources().getDrawable(R.drawable.blue_dot)));
        //myLocationOverlay.setPersonHotspot(1, 1);
        myLocationOverlay.enableMyLocation();
    }

    private void setMapCompass() {
        this.compassOverlay = new CustomCompassOverlay(mContext, new InternalCompassOrientationProvider(mContext),
                map);
        compassOverlay.enableCompass();
    }

    @Override
    public void handleCenterMapClick() {
        if (currentDeviceLocation != null) {
            map.getController().animateTo(currentDeviceLocation);
        }
    }


    @Override
    public void onLocationChanged(GeoPoint location) {
        currentDeviceLocation = location;
        map.getController().animateTo(currentDeviceLocation);
        // TODO:: Remove old markers and Add new marker to location.

    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void drawRoute(Polyline route) {
        map.getOverlays().add(route);
        map.invalidate();
    }

    public void addReport(GeoPoint location, String reportTitle) {
        Drawable nodeIcon = mContext.getResources().getDrawable(R.mipmap.marker_node);
        Marker nodeMarker = new Marker(map);
        nodeMarker.setPosition(location);
        nodeMarker.setIcon(nodeIcon);
        nodeMarker.setTitle(reportTitle);
        map.getOverlays().add(nodeMarker);
    }

    public void showErrorMsg(String msg) {
        Toast.makeText(map.getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
