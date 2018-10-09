package com.cyclist.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.UI.UIManager;
import com.cyclist.logic.FollowLocationService;
import com.cyclist.logic.LocationReceiver;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.models.History;
import com.cyclist.logic.models.Report;
import com.cyclist.logic.models.User;
import com.cyclist.logic.models.UserSettings;
import com.cyclist.logic.search.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.cyclist.logic.common.Constants.BROADCAST_ACTION;

public class MainActivity extends AppCompatActivity {

    private static final int RIDE_TYPES_ID = 1000;
    private static final int ROAD_TYPES_ID = 2000;
    private static final int ROUTE_METHODS_ID = 2000;

    private boolean isSettingsOpen = false;
    private FirebaseUser firebaseUser;
    private MapView mapView;
    private TextView usernameTextView;
    private ImageButton centerMeBtn;
    private ImageButton searchButton;
    private ImageButton reportButton;
    private ImageButton blockedBtn;
    private ImageButton policeBtn;
    private ImageButton distuptionBtn;
    private ImageButton unknownDangerBtn;
    private ImageButton settingsButton;
    private ImageButton logoutButton;
    private ImageButton closeSearchBtn;
    private FrameLayout searchLayout;
    private FrameLayout reportLayout;
    private RelativeLayout settingsLayout;
    private UIManager uiManager;
    private LogicManager logicManager = LogicManager.getInstance();
    private LocationReceiver locationReceiver = new LocationReceiver();
    private IntentFilter intentFilter;
    private Animation searchBarDown;
    private Animation searchBarUp;
    private Animation reportBarDown;
    private Animation reportBarUp;
    private Animation settingsDown;
    private Animation settingsUp;
    private String destination;
    private UserSettings userSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logicManager.initAndAskPermissions(this);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
        setContentView(R.layout.activity_main);
        //inflate and create the map

        intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        uiManager = new UIManager(this);
        logicManager.setListener(uiManager);

        startService(new Intent(this, FollowLocationService.class));

        searchBarDown = AnimationUtils.loadAnimation(this, R.anim.search_bar_down);
        searchBarUp = AnimationUtils.loadAnimation(this, R.anim.search_bar_up);
        reportBarDown = AnimationUtils.loadAnimation(this, R.anim.report_bar_down);
        reportBarUp = AnimationUtils.loadAnimation(this, R.anim.report_bar_up);
        closeSearchBtn = findViewById(R.id.closeSearchBtn);
        searchLayout = findViewById(R.id.searchSlider);
        searchButton = findViewById(R.id.searchBtn);
        reportLayout = findViewById(R.id.reportSlider);
        reportButton = findViewById(R.id.reportBtn);
        usernameTextView = findViewById(R.id.usernameTextView);
        mapView = findViewById(R.id.map);
        centerMeBtn = findViewById(R.id.centerLocationBtn);
        logoutButton = findViewById(R.id.logoutBtn);
        settingsButton = findViewById(R.id.settingsBtn);
        settingsLayout = findViewById(R.id.settingsLayout);
        settingsDown = AnimationUtils.loadAnimation(this, R.anim.settings_down);
        settingsUp = AnimationUtils.loadAnimation(this, R.anim.settings_up);
        uiManager.setMap(mapView);
        searchLayout.setVisibility(View.GONE);
        reportLayout.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.GONE);
        firebaseUser = logicManager.getCurrentUser();
        usernameTextView.setText(logicManager.getUser().getFName());

        createSettingsRadioButtons();
        initializeSearchAddressComponents();
        initializeReportComponents();
        initializeListeners();
    }

    private void initializeReportComponents() {
        blockedBtn = findViewById(R.id.blockedBtn);
        policeBtn = findViewById(R.id.policeBtn);
        distuptionBtn = findViewById(R.id.disruptionBtn);;
        unknownDangerBtn = findViewById(R.id.unknownDangerBtn);

        blockedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReport(Report.ReportDescription.BLOCKED);
            }
        });

        policeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReport(Report.ReportDescription.POLICE);
            }
        });

        distuptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReport(Report.ReportDescription.DISRUPTION);
            }
        });

        unknownDangerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReport(Report.ReportDescription.UNKNOWN_DANGER);
            }
        });
    }

    private void addReport(Report.ReportDescription reportDescription) {
        Report report = Report.builder()
                .description(reportDescription)
                .username(logicManager.getUser().getEmail())
                .isActive(true)
                .time(new Date())
                .destination(uiManager.getCurrentDeviceLocation())
                .build();
        //TODO: saveReport
        hideReportBar();
    }


    private void createSettingsRadioButtons() {
        String[] rides = getResources().getStringArray(R.array.rides);
        userSettings = logicManager.getUserSettings(this);
        RadioGroup radioGroup = findViewById(R.id.ridesTypes);
        for (int i = 0; i < rides.length; i++) {
            RadioButton radioButtonView = new RadioButton(this);
            radioButtonView.setId(RIDE_TYPES_ID + i);
            radioButtonView.setText(rides[i]);
            radioGroup.addView(radioButtonView);
            if(i == userSettings.getRideType().getValue()){
                radioGroup.check(radioButtonView.getId());
            }
        }

        String[] roadTypes = getResources().getStringArray(R.array.road_types);
        radioGroup = findViewById(R.id.roadTypes);
        for (int i = 0; i < roadTypes.length; i++) {
            RadioButton radioButtonView = new RadioButton(this);
            radioButtonView.setId(ROAD_TYPES_ID + i);
            radioButtonView.setText(roadTypes[i]);
            radioButtonView.setPaddingRelative(0,0,50,0);
            radioGroup.addView(radioButtonView);
            if(i == userSettings.getRoadType().getValue()){
                radioGroup.check(radioButtonView.getId());
            }
        }

        String[] routeMethods = getResources().getStringArray(R.array.route_methods);
        radioGroup = findViewById(R.id.routeMethods);
        for (int i = 0; i < routeMethods.length; i++) {
            RadioButton radioButtonView = new RadioButton(this);
            radioButtonView.setId(ROUTE_METHODS_ID + i);
            radioButtonView.setText(routeMethods[i]);
            radioButtonView.setPaddingRelative(0,0,50,0);
            radioGroup.addView(radioButtonView);
            if(i == userSettings.getRouteMethod().getValue()){
                radioGroup.check(radioButtonView.getId());
            }
        }
    }

    private void initializeSearchAddressComponents() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination = place.getName().toString();
                String addressStr = place.getAddress().toString();
                if((addressStr != null) && !(addressStr.equals(""))){
                    List<Address> addressList = null;
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(addressStr , 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    GeoPoint addressGeoPoint = new GeoPoint(address.getLatitude(),address.getLongitude());
                    saveHistory(geocoder);
                    //TODO: send to Ben
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
    }

    private void saveHistory(Geocoder geocoder) {
        History history = new History();
        IGeoPoint currentLocation = logicManager.getCurrentLocation();

        try {
            history.setStartingPoint(geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1).get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }

        history.setDestination(destination);
        history.setEmail(logicManager.getUser().getEmail());
        history.setTime(new Date());
        logicManager.saveHistory(history, this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeListeners() {
        centerMeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.centerLocationBtn:
                        uiManager.handleCenterMapClick();
                        break;
                }
            }
        });



        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsLayer();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logicManager.getAuth().signOut();
                LoginManager.getInstance().logOut();
                if (logicManager.getMGoogleSignInClient() != null) {
                    logicManager.getMGoogleSignInClient().signOut();
                }
                Intent loginActivity = new Intent(MainActivity.this, SignIn.class);
                startActivity(loginActivity);
                finish();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchBar();
            }
        });
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportBar();
            }
        });
        closeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearchBar();
            }
        });

        searchLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float startY = 0;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float endY = event.getY();
                    if (endY < startY) {
                        hideSearchBar();
                    }
                }
                return true;
            }
        });

//        //save for backup
//         mapView.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//
//                if((event.getActionMasked() == MotionEvent.ACTION_UP) && (event.getDownTime() > TimeUnit.SECONDS.toMillis(3))){
//                    long time = TimeUnit.MILLISECONDS.toSeconds(event.getDownTime());
//                    Projection proj = mapView.getProjection();
//                    IGeoPoint touchedPoint = proj.fromPixels((int)event.getX(), (int)event.getY());
//                    touchedPoint.toString();
//
//                }
//                long time = event.getDownTime();
//                return true;
//            }
//        });
    }

    private void showSettingsLayer() {
        settingsLayout.setVisibility(View.VISIBLE);
        settingsLayout.startAnimation(settingsUp);
        isSettingsOpen = true;
    }

    private void showSearchBar() {
        searchLayout.setVisibility(View.VISIBLE);
        searchLayout.startAnimation(searchBarDown);
    }

    private void showReportBar(){
        reportLayout.setVisibility(View.VISIBLE);
        reportLayout.startAnimation(reportBarDown);
    }

    private void hideSearchBar() {
        searchLayout.setVisibility(View.GONE);
        searchLayout.startAnimation(searchBarUp);
        ((InputMethodManager) Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)))
                .hideSoftInputFromWindow(findViewById(R.id.mainLayout)
                        .getWindowToken(), 0);
    }

    private void hideReportBar() {
        reportLayout.setVisibility(View.GONE);
        reportLayout.startAnimation(reportBarUp);
        ((InputMethodManager) Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)))
                .hideSoftInputFromWindow(findViewById(R.id.mainLayout)
                        .getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(locationReceiver, intentFilter);
        uiManager.resumeFollowMe();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(locationReceiver);
        super.onPause();
        uiManager.pauseFollowMe();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser;
        if (logicManager.getCurrentUser() != null) {
            updateUI();
        }
    }

    //TODO: update UI while there is user connected
    private void updateUI() {
    }

    @Override
    public void onBackPressed() {
        if(isSettingsOpen) {
            settingsLayout.setVisibility(View.GONE);
            settingsLayout.startAnimation(settingsDown);
            UserSettings newUserSettings = getNewUserSettings();
            if(!newUserSettings.equals(this.userSettings)){
                logicManager.setUserSettings(newUserSettings, this);
            }
            isSettingsOpen = false;
        }
    }

    @NonNull
    private UserSettings getNewUserSettings() {
        UserSettings userSettings = new UserSettings();
        RadioGroup radioGroup = findViewById(R.id.ridesTypes);
        userSettings.setRideType(User.RideType.getByPosition(radioGroup.getCheckedRadioButtonId() - RIDE_TYPES_ID));
        radioGroup = findViewById(R.id.roadTypes);
        userSettings.setRoadType(Profile.RoadType.getByPosition(radioGroup.getCheckedRadioButtonId() - ROAD_TYPES_ID));
        radioGroup = findViewById(R.id.routeMethods);
        userSettings.setRouteMethod(Profile.RouteMethod.getByPosition(radioGroup.getCheckedRadioButtonId() - ROUTE_METHODS_ID));
        return userSettings;
    }
}
