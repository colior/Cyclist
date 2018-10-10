package com.cyclist.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.models.User;

import java.util.LinkedList;
import java.util.List;

import static com.cyclist.activities.MainActivity.EXTRA_ADDRESS;
import static com.cyclist.activities.MainActivity.EXTRA_DISPLAY_NAME;
import static com.cyclist.activities.MainActivity.HAS_ADDRESS;

public class FavoritesActivity extends AppCompatActivity {

    private LogicManager logicManager;
    public RelativeLayout addFavorite;
    private ListView favoriteListView;
    private List<User.Favorite> favoriteList;
    private List<String> favoriteDisplayNameList;
    private int positionToDelete;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        initializeComponents();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeComponents() {
        logicManager = LogicManager.getInstance();
        favoriteListView = findViewById(R.id.favoriteListView);
        addFavorite = findViewById(R.id.addFavorite);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE){
                    logicManager.getUser().deleteFavorite(positionToDelete);
                    logicManager.saveUser(logicManager.getUser());
                    initializeListView();
                }
            }
        };

        addFavorite.setOnClickListener(view -> addFavorite());
        favoriteListView.setOnItemLongClickListener((parent, view, position, id) -> {
            positionToDelete = position;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.deleteAddresQuestion).setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();
            return true;
        });
        favoriteListView.setOnItemClickListener((parent, view, position, id) -> {
            String address = favoriteList.get(position).getAddress();
            String displayName = favoriteDisplayNameList.get(position);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_ADDRESS, address);
            resultIntent.putExtra(EXTRA_DISPLAY_NAME, displayName);
            setResult(AddWorkActivity.RESULT_OK, resultIntent);
            finish();
        });
        initializeListView();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeListView() {
        favoriteList = logicManager.getUser().getFavorites();
        favoriteDisplayNameList = new LinkedList<>();
        favoriteList.forEach(favorite -> favoriteDisplayNameList.add(favorite.getDisplayName()));
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, R.id.textView, favoriteDisplayNameList);
        favoriteListView.setAdapter(adapter);
    }

    private void addFavorite() {
        Intent addFavoriteActivity = new Intent(FavoritesActivity.this, AddFavoriteActivity.class);
        startActivityForResult(addFavoriteActivity, HAS_ADDRESS);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == HAS_ADDRESS && data != null) {
            initializeListView();
        }
    }
}
