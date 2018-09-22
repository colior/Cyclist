package com.cyclist.logic.models;

import com.cyclist.logic.search.Profile;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings implements Serializable {
    User.RideType rideType;
    Profile.RoadType roadType;
    Profile.RouteMethod routeMethod;
}
