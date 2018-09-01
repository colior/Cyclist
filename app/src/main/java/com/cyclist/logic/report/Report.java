package com.cyclist.logic.report;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {

    public enum ReportDescription{
        BLOCKED,
        POLICE,
        DISRUPTION,
        UNKNOWN_DANGER
    }

    private String username;
    private String destination;
    private ReportDescription description;
    private Date time;
    private boolean isActive;
}
