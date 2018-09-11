package com.cyclist.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.UI.UIManager;
import com.cyclist.logic.FollowLocationService;
import com.cyclist.logic.LocationReceiver;
import com.cyclist.logic.LogicManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

import static android.content.ContentValues.TAG;
import static com.cyclist.logic.common.Constants.BROADCAST_ACTION;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MapView mapView;
    private ImageButton centerMeBtn;
    private UIManager uiManager;
    private LogicManager logicManager = LogicManager.getInstance();
    private LocationReceiver locationReceiver = new LocationReceiver();
    private IntentFilter intentFilter;

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

        mapView = findViewById(R.id.map);
        centerMeBtn = findViewById(R.id.centerLocationBtn);
        centerMeBtn.setOnClickListener(this);
        uiManager.setMap(mapView);
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
        if((currentUser = logicManager.getCurrentUser()) != null){
            updateUI(currentUser);
        }
    }

    //TODO: update UI while there is user connected
    private void updateUI(FirebaseUser currentUser) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.centerLocationBtn:
                uiManager.handleCenterMapClick();
                break;
        }
    }

}
