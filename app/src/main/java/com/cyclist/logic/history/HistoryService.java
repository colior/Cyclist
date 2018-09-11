package com.cyclist.logic.history;

import com.cyclist.logic.firebase.DBService;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryService {

    public static final String HISTORY_BUCKET = "History";
    private static HistoryService instance;
    private DBService dbService = DBService.getInstance();

    public static HistoryService getInstance() {
        if (instance == null){
            instance = new HistoryService();
        }
        return instance;
    }

    public void save(History history){
        dbService.save(history, HISTORY_BUCKET);
    }
}
