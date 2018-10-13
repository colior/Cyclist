package com.cyclist.logic.models;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
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
    private GeoPoint destination;
    private ReportDescription description;
    private Date time;
    private boolean isActive;
}
