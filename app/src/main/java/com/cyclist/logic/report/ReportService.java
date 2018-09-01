package com.cyclist.logic.report;

import com.cyclist.logic.firebase.DBService;

public class ReportService {

    private static final String REPORTS_BUCKET = "Reports";
    private DBService dbService = DBService.getInstance();

    public void save(Report report){
        dbService.save(report, REPORTS_BUCKET);
    }
}
