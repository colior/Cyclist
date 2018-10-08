package com.cyclist.activities;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.models.History;

import java.util.LinkedList;
import java.util.List;

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
        List<String> historyStringList = new LinkedList<>();
        historyList.forEach(history -> historyStringList.add(history.getDestination()));
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, R.id.textView, historyStringList);
        historyListView.setAdapter(adapter);
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String address = (String) historyListView.getItemAtPosition(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("address", address);
                setResult(AddWorkActivity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
