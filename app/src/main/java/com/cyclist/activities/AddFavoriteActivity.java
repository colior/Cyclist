package com.cyclist.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.models.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import static com.cyclist.activities.MainActivity.EXTRA_ADDRESS;
import static com.cyclist.activities.MainActivity.EXTRA_DISPLAY_NAME;

public class AddFavoriteActivity extends AppCompatActivity {
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
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPlaceSelected(Place place) {
                User user = logicManager.getUser();
                User.Favorite favorite = User.Favorite.builder()
                        .address(place.getAddress().toString())
                        .displayName(place.getName().toString())
                        .build();
                user.addFavorite(favorite);
                logicManager.saveUser(user);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_ADDRESS, user.getHome());
                resultIntent.putExtra(EXTRA_DISPLAY_NAME, place.getName());
                setResult(AddWorkActivity.RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onError(Status status) {
            }
        });
    }
}
