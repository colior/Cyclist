package com.cyclist.logic.storage.local;

import android.content.Context;

import com.cyclist.logic.models.History;
import com.cyclist.logic.models.UserSettings;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LocalStorageManager {

    private static final String SEARCH_HISTORY = "SearchHistory";
    private static final String USER_SETTINGS = "UserSettings";
    private static final int MAX_HISTORY_NUMBER = 50;

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
            fileOutputStream = context.openFileOutput(SEARCH_HISTORY, Context.MODE_PRIVATE);
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
            fileInputStream = context.openFileInput(SEARCH_HISTORY);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            history = (List<History>) objectInputStream.readObject();
            fileInputStream.close();
            objectInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    public void saveSettings(UserSettings settings, String uid, Context context){
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(USER_SETTINGS + "_" + uid, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(settings);
            fileOutputStream.close();
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserSettings readSettings(Context context, String uid){
        UserSettings settings = null;
        FileInputStream fileInputStream;
        try {
            fileInputStream = context.openFileInput(USER_SETTINGS + "_" + uid);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            settings = (UserSettings) objectInputStream.readObject();
            fileInputStream.close();
            objectInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return settings;
    }
}
