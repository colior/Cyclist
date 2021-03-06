package com.cyclist.logic.models;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addFavorite(Favorite favoriteToAdd) {
        favorites.forEach(favorite -> {
            if(favoriteToAdd.getDisplayName().equals(favorite.getDisplayName())){
                return;
            }
        });
        favorites.add(favoriteToAdd);
    }

    public void deleteFavorite(int positionToDelete) {
        favorites.remove(positionToDelete);
    }

    public enum RideType
    {
        BIKE(0),
        ELECTRIC_BIKE(1),
        ELECTRIC_SCOOTER(2),
        SEGWAY(3);

        private int position;

        RideType(int position){
            this.position = position;
        }

        public int getValue() {
            return position;
        }

        public static RideType getByPosition(int position){
            for(RideType rideType : RideType.values()){
                if(rideType.getValue() == position){
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
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Favorite
    {
        public String displayName;
        public String address;
    }
}
