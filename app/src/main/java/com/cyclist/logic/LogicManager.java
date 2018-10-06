package com.cyclist.logic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.cyclist.UI.OnLocationChanged;
import com.cyclist.activities.MainActivity;
import com.cyclist.activities.SignIn;
import com.cyclist.logic.common.Constants;
import com.cyclist.logic.models.UserSettings;
import com.cyclist.logic.search.Profile;
import com.cyclist.logic.storage.firebase.DBService;
import com.cyclist.logic.models.History;
import com.cyclist.logic.models.Report;
import com.cyclist.logic.models.User;
import com.cyclist.logic.storage.local.LocalStorageManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import static com.cyclist.logic.common.Constants.HISTORY_BUCKET;
import static com.cyclist.logic.common.Constants.REPORTS_BUCKET;
import static com.cyclist.logic.common.Constants.USERS_BUCKET;

public class LogicManager {
    private static LogicManager instance = null;
    private Activity mActivity;
    private Context mContext;
    private DBService dbService;
    private RoadManager roadManager = new MapQuestRoadManager(Constants.MAPQUEST_KEY);
    //roadManager.addRequestOption("routeType=bicycle");
    private LocationManager mLocationManager;
    private LocalStorageManager localStorageManager;
    @Setter @Getter
    private GoogleSignInClient mGoogleSignInClient;
    private GeoPoint currentLocation;
    private OnLocationChanged listener;
    @Getter
    private User user;
    @Setter
    private SignIn signIn;
    private UserSettings userSettings;

    private LogicManager() {
        dbService = new DBService(this);
        localStorageManager = new LocalStorageManager();
    }

    public UserSettings getUserSettings(Context context) {
        userSettings = localStorageManager.readSettings(context, getAuth().getUid());
        if(userSettings == null) {
            userSettings = new UserSettings(user.getRideType(), Profile.RoadType.CYCLEWAY, Profile.RouteMethod.SAFEST);
        }
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings ,Context context){
        this.userSettings = userSettings;
        localStorageManager.saveSettings(userSettings, getAuth().getUid(), context);
        if(!userSettings.getRideType().equals(user.getRideType())){
            user.setRideType(userSettings.getRideType());
            dbService.save(user, USERS_BUCKET, getAuth().getUid());
        }
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
            dbService.save(report, REPORTS_BUCKET, report.getTime().toString());
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean saveHistory(History history, Context context) {
        try {
            dbService.save(history, HISTORY_BUCKET ,getAuth().getUid() + "/" + history.getTime().toString());
            localStorageManager.saveHistory(history,context);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public List<History> getHistory(Context context){
        return localStorageManager.readHistory(context);
    }

    public OnLocationChanged getOnLocationChangedListener() {
        return listener;
    }

    public FirebaseUser getCurrentUser() {
        return dbService.getCurrentUser();
    }
}

