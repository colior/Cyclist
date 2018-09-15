package com.cyclist.logic.models;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class History {
    private String username;
    private String destination;
    private String startingPoint;
    public Date Time;
}