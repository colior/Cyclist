package com.cyclist.Logic.Model;

import java.util.Date;

import lombok.Data;

@Data
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
