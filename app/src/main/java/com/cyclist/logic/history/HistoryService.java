package com.cyclist.logic.history;

import com.cyclist.logic.firebase.DBService;

public class HistoryService {

    private static final String HISTORY_BUCKET = "History";
    private DBService dbService = DBService.getInstance();

    public void save(History history){
        dbService.save(history, HISTORY_BUCKET);
    }
}
