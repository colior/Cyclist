package com.cyclist.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
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
import com.cyclist.UI.InstructionsFragment;
import com.cyclist.UI.OnNewInstruction;
import com.cyclist.UI.RouteDetailsFragment;
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

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.cyclist.logic.common.Constants.BROADCAST_ACTION;

public class MainActivity extends AppCompatActivity implements OnNewInstruction{

    private static final int RIDE_TYPES_ID = 1000;
    private static final int ROAD_TYPES_ID = 2000;
    private static final int ROUTE_METHODS_ID = 2000;
    public static final int HAS_ADDRESS = 1;
    public static final String EXTRA_DISPLAY_NAME = "displayName";
    public static final String EXTRA_ADDRESS = "address";

    private boolean isSettingsOpen = false;
    private boolean isReportsOpen = false;
    private boolean isSearchOpen = false;
    private MapView mapView;
    private TextView usernameTextView;
    private ImageButton centerMeBtn;
    private ImageButton searchButton;
    private ImageButton settingsButton;
    private ImageButton logoutButton;
    private ImageButton closeSearchBtn;
    private FrameLayout searchLayout;
    private RelativeLayout settingsLayout;
    private UIManager uiManager;
    private LogicManager logicManager = LogicManager.getInstance();
    private LocationReceiver locationReceiver = new LocationReceiver();
    private IntentFilter intentFilter;
    private Animation searchBarDown;
    private Animation searchBarUp;
    private Animation settingsDown;
    private Animation settingsUp;
    private UserSettings userSettings;
    private RelativeLayout homeSearch;
    private RelativeLayout workSearch;
    private RelativeLayout historySearch;
    private RelativeLayout favoriteSearch;
    private ProgressDialog loadingBar;
    private ImageButton reportButton;
    private ImageButton blockedBtn;
    private ImageButton policeBtn;
    private ImageButton distuptionBtn;
    private ImageButton unknownDangerBtn;
    private FrameLayout reportLayout;
    private Animation reportBarDown;
    private Animation reportBarUp;


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

        intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        uiManager = new UIManager(this);
        logicManager.setListener(uiManager);

        startService(new Intent(this, FollowLocationService.class));

        initActivityViews();

        searchBarDown = AnimationUtils.loadAnimation(this, R.anim.search_bar_down);
        searchBarUp = AnimationUtils.loadAnimation(this, R.anim.search_bar_up);
        closeSearchBtn = findViewById(R.id.closeSearchBtn);
        searchLayout = findViewById(R.id.searchSlider);
        settingsLayout = findViewById(R.id.settingsLayout);
        searchButton = findViewById(R.id.searchBtn);
        usernameTextView = findViewById(R.id.usernameTextView);
        mapView = findViewById(R.id.map);
        centerMeBtn = findViewById(R.id.centerLocationBtn);
        logoutButton = findViewById(R.id.logoutBtn);
        settingsButton = findViewById(R.id.settingsBtn);
        homeSearch = findViewById(R.id.homeSearch);
        workSearch = findViewById(R.id.workSearch);
        favoriteSearch = findViewById(R.id.favoriteSearch);
        historySearch = findViewById(R.id.historySearch);
        settingsDown = AnimationUtils.loadAnimation(this, R.anim.settings_down);
        settingsUp = AnimationUtils.loadAnimation(this, R.anim.settings_up);
        uiManager.setMap(mapView);
        searchLayout.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.GONE);
        usernameTextView.setText(logicManager.getUser().getFName());
        reportBarDown = AnimationUtils.loadAnimation(this, R.anim.report_bar_down);
        reportBarUp = AnimationUtils.loadAnimation(this, R.anim.report_bar_up);
        reportLayout = findViewById(R.id.reportSlider);
        reportButton = findViewById(R.id.reportBtn);
        reportLayout.setVisibility(View.GONE);


        initializeReportComponents();
        createSettingsRadioButtons();
        initializeSearchAddressComponents();
        initializeListeners();
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


    private void saveHistory(Geocoder geocoder, String destination, String displayName) {
        History history = new History();
        IGeoPoint currentLocation = logicManager.getCurrentLocation();

        try {
            history.setStartingPoint(geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1).get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }

        history.setDisplayName(displayName);
        history.setDestination(destination);
        history.setEmail(logicManager.getUser().getEmail());
        history.setTime(new Date());
        logicManager.saveHistory(history, this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeListeners() {

        homeSearch.setOnLongClickListener(v -> {
            chooseHomeAddress();
            return true;
        });

        homeSearch.setOnClickListener(v -> {
            String home = logicManager.getUser().getHome();
            if(home != null && !home.isEmpty()){
                selectRoute(home, home);
            }
            else {
                chooseHomeAddress();
            }
        });

        workSearch.setOnLongClickListener(v -> {
            chooseWorkAddress();
            return true;
        });

        workSearch.setOnClickListener(v -> {
            String work = logicManager.getUser().getWork();
            if(work != null && !work.isEmpty()){
                selectRoute(work, work);
            }
            else {
                chooseWorkAddress();
            }
        });

        favoriteSearch.setOnClickListener(v -> {
            chooseFavorite();
        });

        historySearch.setOnClickListener(v -> {
            if(!logicManager.getHistory(MainActivity.this).isEmpty()) {
                chooseHistoryAddress();
            }
            else {
                AlertDialog alert = new AlertDialog.Builder(MainActivity.this).setMessage("You have no history yet").setTitle("No History").create();
                alert.show();
            }
        });

        centerMeBtn.setOnClickListener(v -> uiManager.handleCenterMapClick());
        settingsButton.setOnClickListener(v -> showSettingsLayer());

        logoutButton.setOnClickListener(v -> {
            logicManager.getAuth().signOut();
            LoginManager.getInstance().logOut();
            if(logicManager.getMGoogleSignInClient() != null) {
                logicManager.getMGoogleSignInClient().signOut();
            }
            Intent loginActivity = new Intent(MainActivity.this, SignIn.class);
            startActivity(loginActivity);
            finish();
        });

        searchButton.setOnClickListener(v -> showSearchBar());

        closeSearchBtn.setOnClickListener(v -> hideSearchBar());

        searchLayout.setOnTouchListener((v, event) -> {
            float startY = 0;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float endY = event.getY();
                if (endY < startY) {
                    hideSearchBar();
                }
            }
            return true;
        });

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportBar();
            }
        });

    }

    private void chooseFavorite() {
        Intent favoriteActivity = new Intent(MainActivity.this, FavoritesActivity.class);
        startActivityForResult(favoriteActivity, HAS_ADDRESS);
    }

    private void chooseHistoryAddress() {
        Intent historyAddressActivity = new Intent(MainActivity.this, HistoryActivity.class);
        startActivityForResult(historyAddressActivity, HAS_ADDRESS);
    }

    private void chooseWorkAddress() {
        Intent addWorkActivity = new Intent(MainActivity.this, AddWorkActivity.class);
        startActivityForResult(addWorkActivity, HAS_ADDRESS);
    }

    private void chooseHomeAddress() {
        Intent addHomeActivity = new Intent(MainActivity.this, AddHomeActivity.class);
        startActivityForResult(addHomeActivity, HAS_ADDRESS);
    }

    private void showSettingsLayer() {
        settingsLayout.setVisibility(View.VISIBLE);
        settingsLayout.startAnimation(settingsUp);
        isSettingsOpen = true;
    }

    private void hideSettingsLayer() {
        settingsLayout.setVisibility(View.GONE);
        settingsLayout.startAnimation(settingsDown);
        UserSettings newUserSettings = getNewUserSettings();
        if(!newUserSettings.equals(this.userSettings)){
            logicManager.setUserSettings(newUserSettings, this);
        }
        isSettingsOpen = false;
    }

    private void showSearchBar() {
        searchLayout.setVisibility(View.VISIBLE);
        searchLayout.startAnimation(searchBarDown);
        isSearchOpen = true;
    }

    private void hideSearchBar() {
        searchLayout.setVisibility(View.GONE);
        searchLayout.startAnimation(searchBarUp);
        ((InputMethodManager) Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)))
                .hideSoftInputFromWindow(findViewById(R.id.mainLayout)
                        .getWindowToken(), 0);
        isSearchOpen = false;
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

    private void initActivityViews() {
        searchBarUp = AnimationUtils.loadAnimation(this, R.anim.settings_up);
        searchBarDown = AnimationUtils.loadAnimation(this, R.anim.settings_down);
        closeSearchBtn = findViewById(R.id.closeSearchBtn);
        searchLayout = findViewById(R.id.searchSlider);
        searchButton = findViewById(R.id.searchBtn);
        usernameTextView = findViewById(R.id.usernameTextView);
        mapView = findViewById(R.id.map);
        centerMeBtn = findViewById(R.id.centerLocationBtn);
        logoutButton = findViewById(R.id.logoutBtn);
        settingsButton = findViewById(R.id.settingsBtn);
        uiManager.setMap(mapView);
        searchLayout.setVisibility(View.GONE);
        usernameTextView.setText(logicManager.getUser().getFName());
        uiManager.setInstructionListener(this);
    }

    private void initializeSearchAddressComponents() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                enableLoadingBar();
                String addressStr = place.getAddress().toString();
                String displayName = place.getName().toString();
                selectRoute(addressStr, displayName);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                uiManager.showErrorMsg(status.getStatusMessage());
            }
        });
    }

    private void selectRoute(String addressStr, String displayName) {
        if(!(addressStr.isEmpty())){
            List<Address> addressList = null;
            Geocoder geocoder = new Geocoder(MainActivity.this);
            try {
                addressList = geocoder.getFromLocationName(addressStr , 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            saveHistory(geocoder, addressStr, displayName);
            ArrayList<GeoPoint> destinationList = new ArrayList<>();
            destinationList.add(new GeoPoint(address.getLatitude(),address.getLongitude()));
            hideSearchBar();
            disableProgressBar();
            uiManager.showDestinationAndWaitForOk(destinationList);
        }
    }

    @Override
    public void onBackPressed() {
        if(isSettingsOpen) {
            hideSettingsLayer();
        }
        else if(isSearchOpen){
            hideSearchBar();
        }
        else if(isReportsOpen){
            hideReportBar();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == HAS_ADDRESS && data != null) {
            enableLoadingBar();
            String address = data.getStringExtra(EXTRA_ADDRESS);
            String displayName = data.getStringExtra(EXTRA_DISPLAY_NAME);
            selectRoute(address, displayName);
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


    @Override
    public void setInstructionBar(InstructionsFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void setRouteDetailsBar(RouteDetailsFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void hideRoutingFragments() {
        findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
        findViewById(R.id.bottomFragmentContainer).setVisibility(View.GONE);
    }

    @Override
    public void showRoutingFragments() {
        disableProgressBar();
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.bottomFragmentContainer).setVisibility(View.VISIBLE);
    }

    private void enableLoadingBar() {
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle(R.string.app_name);
        loadingBar.setMessage(getString(R.string.please_wait));
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
    }

    public void disableProgressBar() {
        if(loadingBar != null) {
            loadingBar.hide();
        }
    }

    private void initializeReportComponents() {
        blockedBtn = findViewById(R.id.blockedBtn);
        policeBtn = findViewById(R.id.policeBtn);
        distuptionBtn = findViewById(R.id.disruptionBtn);
        unknownDangerBtn = findViewById(R.id.unknownDangerBtn);
        blockedBtn.setOnClickListener(v -> addReport(Report.ReportDescription.BLOCKED));
        policeBtn.setOnClickListener(v -> addReport(Report.ReportDescription.POLICE));
        distuptionBtn.setOnClickListener(v -> addReport(Report.ReportDescription.DISRUPTION));
        unknownDangerBtn.setOnClickListener(v -> addReport(Report.ReportDescription.UNKNOWN_DANGER));
    }

    private void addReport(Report.ReportDescription reportDescription) {
        Report report = Report.builder()
                .description(reportDescription)
                .username(logicManager.getUser().getEmail())
                .isActive(true)
                .time(new Date())
                .destination(uiManager.getCurrentDeviceLocation())
                .build();
        logicManager.saveReport(report);
        hideReportBar();
        Toast.makeText(this, R.string.added_report,
                Toast.LENGTH_SHORT).show();
    }


    private void showReportBar(){
        reportLayout.setVisibility(View.VISIBLE);
        reportLayout.startAnimation(reportBarDown);
        isReportsOpen = true;
    }
    private void hideReportBar() {
        reportLayout.setVisibility(View.GONE);
        reportLayout.startAnimation(reportBarUp);
        ((InputMethodManager) Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)))
                .hideSoftInputFromWindow(findViewById(R.id.mainLayout)
                        .getWindowToken(), 0);
        isReportsOpen = false;
    }



}
