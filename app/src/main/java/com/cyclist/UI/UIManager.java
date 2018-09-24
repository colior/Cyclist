package com.cyclist.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Locale;

import static org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK;

public class UIManager implements OnCenterMeClick, OnLocationChanged, OnRoadCalculated {

    private Context mContext;
    private MapView map;
    private GeoPoint currentDeviceLocation;
    private MyLocationNewOverlay myLocationOverlay;
    private CompassOverlay compassOverlay;
    private RotationGestureOverlay rotationGestureOverlay;
    private Polyline shownRoute;
    private Pair<Integer, String> lastPair;
    private Boolean routeMode;
    private InstructionsFragment fragment = InstructionsFragment.newInstance(null,null);
    private OnNewInstruction instructionListener;

    public UIManager(Context mContext) {
        this.mContext = mContext;
    }


    public void setMap(MapView map) {
        this.map = map;
//        if (ThunderforestTileSource.haveMapId(mContext)) {
//            ThunderforestTileSource tileSource = new ThunderforestTileSource(mContext, ThunderforestTileSource.CYCLE);
//            TileSourceFactory.addTileSource(tileSource);
//            map.setTileSource(TileSourceFactory.getTileSource(ThunderforestTileSource.mapName(ThunderforestTileSource.CYCLE)));
//        } else {
        map.setTileSource(MAPNIK);
//        }
        initMap();
    }

    private void initMap() {
        map.setTilesScaledToDpi(true);
        map.setFlingEnabled(true);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        IMapController mapController = map.getController();
        mapController.setCenter(LogicManager.getInstance().getCurrentLocation());
        mapController.setZoom(17d);


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
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void drawRoute(Polyline route, BoundingBox bounds) {
        clearRoute();
        map.getOverlays().add(route);
        map.zoomToBoundingBox(bounds, true);
        shownRoute = route;
        map.invalidate();
    }


    @Override
    public void promptRouteDetails(final Road road, final Polyline overlay) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(mContext);
        StringBuilder sb = new StringBuilder();

        sb.append(mContext.getResources().getString(R.string.route_length_text))
                .append(String.format(Locale.getDefault(), " %02.3f km", road.mLegs.get(0).mLength))
                .append(System.lineSeparator())
                .append(mContext.getResources().getString(R.string.duration_text))
                .append(getTimeString(road.mLegs.get(0).mDuration));

        dlgBuilder.setMessage(mContext.getResources().getString(R.string.route_title));
        dlgBuilder.setMessage(sb.toString())
                .setPositiveButton(mContext.getResources().getString(R.string.go_text), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    setRouteMode(true);
                    drawRoute(overlay, road.mBoundingBox);
                });
        AlertDialog dlg = dlgBuilder.create();
        dlg.show();
    }

    private String getTimeString(double duration) {
        double totalMinutes = duration / 60;
        Double minutes = totalMinutes % 60;
        Double hours = totalMinutes / 60;
        if (hours > 1) {
            return String.format(Locale.getDefault(), " %1d:%2d", hours.intValue(), minutes.intValue());
        } else {
            return String.format(Locale.getDefault(), " %1d %s", minutes.intValue(), mContext.getResources().getString(R.string.minutes_text));
        }
    }

    public void clearRoute() {
        if (shownRoute != null) {
            map.getOverlays().remove(shownRoute);
        }
        shownRoute = null;
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

    public void setRouteMode(Boolean value) {
        if (value) {
            LogicManager.getInstance().setRouteMode(true);
        } else {
            clearRouteMode();
            this.routeMode = value;
        }
    }

    @Override
    public void showNewInstruction(Pair<Integer, String> pair) {
        if (pair == null)
            return;
        if (lastPair != null && lastPair.second.equals(pair.second))
            return;
        lastPair = pair;
        fragment = InstructionsFragment.newInstance(pair.first, pair.second);
        instructionListener.setInstructionBar(fragment);
    }

    @Override
    public MapView getMapView() {
        return map;
    }

    private void clearRouteMode() {

    }

    public void setInstructionListener(OnNewInstruction instructionListener) {
        this.instructionListener = instructionListener;
    }
}
