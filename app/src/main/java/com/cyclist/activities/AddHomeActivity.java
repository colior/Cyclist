package com.cyclist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import static android.content.Intent.ACTION_CLOSE_SYSTEM_DIALOGS;

public class AddHomeActivity extends AppCompatActivity {

    LogicManager logicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        logicManager = LogicManager.getInstance();
        initializeSearchAddressComponents();
    }

    private void initializeSearchAddressComponents() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                logicManager.getUser().setHome(place.getAddress().toString());
                logicManager.saveUser(logicManager.getUser());
                finish();
            }

            @Override
            public void onError(Status status) {
            }
        });
    }

}
