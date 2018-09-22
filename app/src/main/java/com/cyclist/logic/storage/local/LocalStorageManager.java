package com.cyclist.logic.storage.local;

import android.content.Context;

import com.cyclist.logic.models.History;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LocalStorageManager {

    private static final String searchHistory = "SearchHistory";
    private static final int MAX_HISTORY_NUMBER = 10;

    public void saveHistory(History history, Context context){
        FileOutputStream fileOutputStream;
        List<History> historyList = readHistory(context);
        if(historyList == null){
            historyList = new LinkedList<>();
        }
        historyList.add(0, history);
        if(historyList.size() > MAX_HISTORY_NUMBER){
            historyList.remove(MAX_HISTORY_NUMBER);
        }

        try {
            fileOutputStream = context.openFileOutput(searchHistory, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(historyList);
            fileOutputStream.close();
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<History> readHistory(Context context){
        List<History> history = null;
        FileInputStream fileInputStream;
        try {
            fileInputStream = context.openFileInput(searchHistory);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            history = (List<History>) objectInputStream.readObject();
            fileInputStream.close();
            objectInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }
}
