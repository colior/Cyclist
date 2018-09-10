package com.cyclist.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.UI.UIManager;
import com.cyclist.logic.LogicManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private MapView map;
    private UIManager uiManager = new UIManager();
    private LogicManager logicManager = LogicManager.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logicManager.setUpLocationListner(this);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
        setContentView(R.layout.activity_main);
        //inflate and create the map
        uiManager.setMap((MapView) findViewById(R.id.map));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
    }
}
