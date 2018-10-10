package com.cyclist.activities;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.models.History;

import java.util.LinkedList;
import java.util.List;

import static com.cyclist.activities.MainActivity.EXTRA_ADDRESS;
import static com.cyclist.activities.MainActivity.EXTRA_DISPLAY_NAME;

public class HistoryActivity extends AppCompatActivity {

    private LogicManager logicManager;
    private ListView historyListView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initializeComponents();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeComponents() {
        logicManager = LogicManager.getInstance();
        historyListView = findViewById(R.id.historyListView);
        List<History> historyList = logicManager.getHistory(this);
        List<String> historyDisplayNameList = new LinkedList<>();
        List<String> historyAddressList = new LinkedList<>();
        historyList.forEach(history ->  {
            historyAddressList.add(history.getDestination());
            historyDisplayNameList.add(history.getDisplayName());
        });
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, R.id.textView, historyDisplayNameList);
        historyListView.setAdapter(adapter);
        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            String address = historyAddressList.get(position);
            String displayName = historyDisplayNameList.get(position);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_ADDRESS, address);
            resultIntent.putExtra(EXTRA_DISPLAY_NAME, displayName);
            setResult(AddWorkActivity.RESULT_OK, resultIntent);
            finish();
        });
    }
}
