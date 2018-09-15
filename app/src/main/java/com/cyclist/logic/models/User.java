package com.cyclist.logic.models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    public enum RideType
    {
        BIKE("Bicycle"),
        ELECTRIC_BIKE("Electric Bicycle"),
        ELECTRIC_SCOOTER("Electric Scooter"),
        SEGWAY("Segway");

        private String displayName;

        RideType(String displayName){
            this.displayName = displayName;
        }

        public String getValue() {
            return displayName;
        }

        public static RideType getByDisplayName(String displayName){
            for(RideType rideType : RideType.values()){
                if(rideType.getValue().equals(displayName)){
                    return rideType;
                }
            }
            return null;
        }
    }

    private String email;
    private String fName;
    private String lName;
    private int km;
    private Date birthday;
    private String home;
    private String work;
    private RideType rideType;
    private List<Favorite> favorites = new LinkedList<>();

    @Data
    public class Favorite
    {
        public String label;
        public String address;
    }
}
