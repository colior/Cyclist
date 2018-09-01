package com.cyclist.Logic.Model;

import java.util.Date;

import lombok.Data;

@Data
public class History {
    private String username;
    private String destination;
    private String startingPoint;
    public Date Time;
}