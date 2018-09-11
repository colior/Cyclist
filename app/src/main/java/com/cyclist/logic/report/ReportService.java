package com.cyclist.logic.report;

import com.cyclist.logic.firebase.DBService;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportService {

    public static final String REPORTS_BUCKET = "Reports";
    private static ReportService instance;
    private DBService dbService = DBService.getInstance();

    public static ReportService getInstance() {
        if (instance == null){
            instance = new ReportService();
        }
        return instance;
    }

    public void save(Report report){
        dbService.save(report, REPORTS_BUCKET);
    }
}
