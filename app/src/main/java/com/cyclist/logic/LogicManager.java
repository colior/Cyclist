package com.cyclist.logic;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.cyclist.UI.OnLocationChanged;
import com.cyclist.logic.common.Constants;
import com.cyclist.logic.firebase.DBService;
import com.cyclist.logic.history.History;
import com.cyclist.logic.history.HistoryService;
import com.cyclist.logic.report.Report;
import com.cyclist.logic.report.ReportService;
import com.cyclist.logic.user.User;
import com.cyclist.logic.user.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import static com.cyclist.logic.common.Constants.BROADCAST_ACTION;
import static com.cyclist.logic.common.Constants.LAT_TAG;
import static com.cyclist.logic.common.Constants.LONG_TAG;

public class LogicManager {
    private static LogicManager instance = null;
    private Activity mActivity;
    private Context mContext;
    private DBService dbService;
    private UserService userService = new UserService();
    private HistoryService historyService = new HistoryService();
    private ReportService reportService = new ReportService();
    private RoadManager roadManager = new MapQuestRoadManager(Constants.MAPQUEST_KEY);
    //roadManager.addRequestOption("routeType=bicycle");
    private LocationManager mLocationManager;
    private GeoPoint currentLocation;
    private OnLocationChanged listener;

    private LogicManager() {
        dbService = DBService.getInstance();
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

    public void initAndAskPermissions(Activity activity) {
        mContext = mActivity = activity;
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
                //Ask for perrmission
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
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
    }

    public FirebaseAuth getAuth() {
        return dbService.getMAuth();
    }

    public FirebaseUser getCurrentUser() {
        return dbService.getCurrentUser();
    }

    public boolean saveUser(User user) {
        try {
            userService.save(user);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean saveReport(Report report) {
        try {
            reportService.save(report);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean saveHistory(History history) {
        try {
            historyService.save(history);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public OnLocationChanged getOnLocationChangedListener() {
        return listener;
    }
}
