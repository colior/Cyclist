package com.cyclist.logic.usernetwork.user;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class User {

    public enum RideType
    {
        BIKE,
        ELECTRIC_BIKE,
        ELECTRIC_SCOOTER,
        SEGWAY
    }

    private String username;
    private String password;
    private String fName;
    private String lName;
    private int km;
    private Date birthday;
    private RideType ride;
    private String home;
    private String work;
    private List<Favorite> favorites = new LinkedList<>();

    @Data
    public class Favorite
    {
        public String label;
        public String address;
    }
}
