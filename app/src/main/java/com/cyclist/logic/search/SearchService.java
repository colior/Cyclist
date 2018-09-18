package com.cyclist.logic.search;

import android.location.Address;
import android.location.Geocoder;

import com.cyclist.activities.MainActivity;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;

public class SearchService {

    public GeoPoint convertAddressToGeopoint(String addressStr, Geocoder geocoder) {
        GeoPoint addressGeoPoint = null;
        if ((addressStr != null) && !(addressStr.equals(""))) {
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocationName(addressStr, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            addressGeoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
        }
        return addressGeoPoint;
    }

}
