package com.cyclist.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cyclist.R;
import com.cyclist.UI.UIManager;
import com.cyclist.logic.FollowLocationService;
import com.cyclist.logic.LocationReceiver;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.RoutePoints;
import com.cyclist.logic.getRoadAsync;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cyclist.logic.common.Constants.BROADCAST_ACTION;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private MapView mapView;
    private TextView usernameTextView;
    private ImageButton centerMeBtn;
    private ImageButton searchButton;
    private ImageButton settingsButton;
    private ImageButton logoutButton;
    private ImageButton closeSearchBtn;
    private FrameLayout searchLayout;
    private UIManager uiManager;
    private LogicManager logicManager = LogicManager.getInstance();
    private LocationReceiver locationReceiver = new LocationReceiver();
    private IntentFilter intentFilter;
    private Animation animUp;
    private Animation animDown;
    private String destination;

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

        animUp = AnimationUtils.loadAnimation(this, R.anim.anim_up);
        animDown = AnimationUtils.loadAnimation(this, R.anim.anim_down);
        closeSearchBtn = findViewById(R.id.closeSearchBtn);
        searchLayout = findViewById(R.id.searchSlider);
        searchButton = findViewById(R.id.searchBtn);
        usernameTextView = findViewById(R.id.usernameTextView);
        mapView = findViewById(R.id.map);
        centerMeBtn = findViewById(R.id.centerLocationBtn);
        centerMeBtn.setOnClickListener(this);
        logoutButton = findViewById(R.id.logoutBtn);
        settingsButton = findViewById(R.id.settingsBtn);
        uiManager.setMap(mapView);
        searchLayout.setVisibility(View.GONE);
        firebaseUser = logicManager.getCurrentUser();
        usernameTextView.setText(logicManager.getUser().getFName());

        GeoPoint start = new GeoPoint(32.070122d, 34.771721d);
        GeoPoint end = new GeoPoint(32.047532d, 34.759606d);
        ArrayList<GeoPoint> points = new ArrayList();
        points.add(start);
        points.add(end);
        RoutePoints routePoints = new RoutePoints(points);
        new getRoadAsync(uiManager, ContextCompat.getColor(this, R.color.colorRoute)).execute(routePoints);
        uiManager.addReport(start, "this is the start point");

        initializeSearchAddressComponents();
        initializeListeners();
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

                    //TODO: send to Ben
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
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

            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logicManager.getAuth().signOut();
                LoginManager.getInstance().logOut();
                if(logicManager.getMGoogleSignInClient() != null) {
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
    }

    private void showSearchBar() {
        searchLayout.setVisibility(View.VISIBLE);
        searchLayout.startAnimation(animUp);
    }

    private void hideSearchBar() {
        searchLayout.setVisibility(View.GONE);
        searchLayout.startAnimation(animDown);
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

}
