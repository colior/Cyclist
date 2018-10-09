package com.cyclist.logic.models;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class History implements Serializable {
    private String email;
    private String destination;
    private String startingPoint;
    public Date time;
}