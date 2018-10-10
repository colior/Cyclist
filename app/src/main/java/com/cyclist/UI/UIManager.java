package com.cyclist.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.common.Utils;

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

import java.util.ArrayList;
import java.util.Locale;

import static org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK;

public class UIManager implements OnCenterMeClick, OnLocationChanged, OnRoadCalculated, RouteDetailsFragment.closeListener {

    private Context mContext;
    private MapView map;
    private GeoPoint currentDeviceLocation;
    private MyLocationNewOverlay myLocationOverlay;
    private CompassOverlay compassOverlay;
    private RotationGestureOverlay rotationGestureOverlay;
    private Polyline shownRoute;
    private Pair<Integer, String> lastPair;
    private Boolean routeMode = false;
    private OnNewInstruction instructionListener;
    private Marker destinationMarker;
    private boolean firstLunch = true;

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
        pauseFollowMe();
    }

    public void pauseFollowMe() {
        this.myLocationOverlay.disableFollowLocation();
    }

    public void resumeFollowMe() {
        this.myLocationOverlay.enableFollowLocation();
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
        if (firstLunch){
            map.getController().animateTo(currentDeviceLocation);
            firstLunch = false;
        }
        if (routeMode) {
            map.getController().animateTo(currentDeviceLocation);
        }
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
                .append(Utils.getTimeString(road.mLegs.get(0).mDuration, mContext.getResources().getString(R.string.minutes_text)));

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

    @Override
    public void showRouteDetailsBar(Double length, Double duration) {
        RouteDetailsFragment routeDetailsFragment = RouteDetailsFragment.newInstance(duration, length);
        routeDetailsFragment.setListener(this);
        instructionListener.setRouteDetailsBar(routeDetailsFragment);
    }

    public void clearRoute() {
        if (shownRoute != null) {
            map.getOverlays().remove(shownRoute);
            map.getOverlays().remove(destinationMarker);
        }
        shownRoute = null;
    }

    public void showErrorMsg(String msg) {
        Toast.makeText(map.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void setRouteMode(Boolean value) {
        if (value) {
            showRouteMode();
            routeMode = true;
        } else {
            clearRouteMode();
            this.routeMode = false;
        }
    }

    private void showRouteMode() {
        LogicManager.getInstance().setRouteMode(true);
        instructionListener.showRoutingFragments();
        resumeFollowMe();
    }

    private void clearRouteMode() {
        LogicManager.getInstance().setRouteMode(false);
        instructionListener.hideRoutingFragments();
        pauseFollowMe();
        clearRoute();
    }

    @Override
    public void showNewInstruction(Pair<Integer, String> pair) {
        if (pair == null)
            return;
        if (lastPair != null && lastPair.second.equals(pair.second))
            return;
        lastPair = pair;
        InstructionsFragment instructionsFragment = InstructionsFragment.newInstance(pair.first, pair.second);
        instructionListener.setInstructionBar(instructionsFragment);
    }

    @Override
    public MapView getMapView() {
        return map;
    }



    public void setInstructionListener(OnNewInstruction instructionListener) {
        this.instructionListener = instructionListener;
    }

    public void showDestinationAndWaitForOk(ArrayList<GeoPoint> destinationList) {
        Drawable nodeIcon = mContext.getResources().getDrawable(R.drawable.marker_destination);
        destinationMarker = new Marker(map);
        destinationMarker.setIcon(nodeIcon);
        GeoPoint destination = destinationList.get(destinationList.size() - 1);
        destinationMarker.setPosition(destination);
        map.getOverlayManager().add(destinationMarker);
        map.getController().setCenter(destination);
        map.invalidate();
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(mContext);

        dlgBuilder.setMessage(mContext.getResources().getString(R.string.destination_text))
                .setPositiveButton(mContext.getResources().getString(R.string.continue_text), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    LogicManager manager = LogicManager.getInstance();
                    manager.buildRoute(this, destinationList, true);
                });
        dlgBuilder.setOnCancelListener(dialogInterface -> {
            map.getOverlayManager().remove(destinationMarker);
            LogicManager manager = LogicManager.getInstance();
            map.getController().animateTo(manager.getCurrentLocation());
        });

        AlertDialog dlg = dlgBuilder.create();
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dlg.getWindow().getAttributes();

        wmlp.gravity = Gravity.TOP;
        dlg.show();
    }

    @Override
    public void onCloseClick() {
        setRouteMode(false);
    }
}
