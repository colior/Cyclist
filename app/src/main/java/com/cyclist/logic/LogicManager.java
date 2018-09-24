package com.cyclist.logic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;

import com.cyclist.R;
import com.cyclist.UI.OnLocationChanged;
import com.cyclist.UI.UIManager;
import com.cyclist.activities.SignIn;
import com.cyclist.logic.common.Constants;
import com.cyclist.logic.firebase.DBService;
import com.cyclist.logic.models.History;
import com.cyclist.logic.models.Report;
import com.cyclist.logic.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

import static com.cyclist.logic.common.Constants.HISTORY_BUCKET;
import static com.cyclist.logic.common.Constants.REPORTS_BUCKET;

//import com.cyclist.R;

public class LogicManager {
    private static LogicManager instance = null;
    private Activity mActivity;
    private Context mContext;
    private DBService dbService;
    private RoadManager roadManager = new MapQuestRoadManager(Constants.MAPQUEST_KEY);
    @Setter @Getter
    private GoogleSignInClient mGoogleSignInClient;
    private GeoPoint currentLocation;
    private Road presentedRoad;
    private int roadColor;
    private OnLocationChanged listener;
    private Boolean routeMode = false;
    @Getter
    private User user;
    @Setter
    private SignIn signIn;
    private final double DISTANCE = 20d;

    private LogicManager() {
        dbService = new DBService(this);
        roadManager.addRequestOption(Constants.CYCLEWAY_TAG);
    }

    public static LogicManager getInstance() {
        if (instance == null) {
            synchronized (LogicManager.class) {
                if (instance == null) {
                    instance = new LogicManager();
                    return instance;
                }
            }
        }
        return instance;
    }

    public void setUser(User user){
        this.user = user;
        if(user != null) {
            signIn.onUserSignedIn();
        }
    }

    public void connect(){
        dbService.getUser();
    }

    public void initAndAskPermissions(Activity activity) {
        mContext = mActivity = activity;
        setContextVariables();
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Ask for permission's
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void setContextVariables() {
        roadColor = ContextCompat.getColor(mContext, R.color.colorRoute);
    }

    public void setListener(OnLocationChanged listener) {
        this.listener = listener;
    }

    public IGeoPoint getCurrentLocation() {
        if (currentLocation != null)
            return currentLocation;
        else
            return null;
        //TODO CHANGE !!!
    }

    public void setCurrentLocation(GeoPoint currentLocation) {
        this.currentLocation = currentLocation;
        if (routeMode){
            listener.showNewInstruction(getCurrentRouteInstruction(listener.getMapView()));
        }
    }

    public FirebaseAuth getAuth() {
        return dbService.getMAuth();
    }


    public boolean saveUser(User user) {
        try {
            dbService.saveNewUser(user);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    public boolean saveReport(Report report) {
        try {
            dbService.save(report, REPORTS_BUCKET);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean saveHistory(History history) {
        try {
            dbService.save(history, HISTORY_BUCKET);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public OnLocationChanged getOnLocationChangedListener() {
        return listener;
    }

    public FirebaseUser getCurrentUser() {
        return dbService.getCurrentUser();
    }

    public RoadManager getRoadManger() {
        return roadManager;
    }

    public void setPresentedRoad(Road presentedRoad) {
        this.presentedRoad = presentedRoad;
    }

    public void buildRoute(UIManager uiManager, ArrayList<GeoPoint> geoPointList, Boolean startFromCurrentLocation){
        if (startFromCurrentLocation){
            geoPointList.add(0, currentLocation);
        }
        RoutePoints routePoints = new RoutePoints(geoPointList);
        new getRoadAsync(uiManager, roadColor).execute(routePoints);
    }

    private Pair<Integer, String> getCurrentRouteInstruction(MapView mapView){
        RoadNode closestNode = findClosestNode(mapView);
        if (closestNode == null){
            return null;
        }
        Integer maneuverResId = getManeuverResourceId(closestNode.mManeuverType);
        return new Pair<>(maneuverResId, closestNode.mInstructions);
    }

    private Integer getManeuverResourceId(int maneuverType) {
//        TODO:: Implement switch base on :
//          https://developer.mapquest.com/documentation/nav-sdk/android/v3.4/javadoc/com/mapquest/navigation/model/Maneuver.Type.html#DESTINATION
//          https://github.com/MKergall/osmbonuspack/tree/master/OSMNavigator/src/main/res/drawable-mdpi
//        switch (maneuverType){
//            case MANEUVER.CONTINUE;
//        }
        return R.drawable.ic_continue;
    }

    private RoadNode findClosestNode(MapView mapView) {
        RoadNode result = presentedRoad.mNodes.get(0);
        double distance = currentLocation.distanceToAsDouble(result.mLocation);
        for (int i = 1; i < presentedRoad.mNodes.size(); i++) {
            RoadNode edgeStart = presentedRoad.mNodes.get(i);
//            RoadNode edgeEnd = presentedRoad.mNodes.get(i + 1);
//            Polyline edge = makeEdge(edgeStart, edgeEnd);

            Double newDistance = currentLocation.distanceToAsDouble(edgeStart.mLocation);
            if (newDistance < distance) {
                result = edgeStart;
                distance = newDistance;
            }
        }
        return result;
//        if (checkReRoute()){
//            //listener.showReRoute();
//            // TODO:: Implement
//        }
//        return null;
    }

    private Polyline makeEdge(RoadNode edgeStart, RoadNode edgeEnd) {
        Polyline res = new Polyline();
        res.addPoint(edgeStart.mLocation);
        res.addPoint(edgeEnd.mLocation);
        return res;
    }

    private Boolean checkReRoute() {
//        double distance = Distance.getSquaredDistanceToLine(currentLocation.getLatitude(), currentLocation.getLongitude(),
//                edgeStart.mLocation.getLatitude(), edgeStart.mLocation.getLongitude(),
//                edgeEnd.mLocation.getLatitude(), edgeEnd.mLocation.getLongitude());
//        if (edge.isCloseTo(currentLocation, testDistance, mapView)) {
//            return edgeStart;
//        }
        return false;
    }

    public void setRouteMode(Boolean routeMode) {
        this.routeMode = routeMode;
    }

}

