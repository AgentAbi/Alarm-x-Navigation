package com.myapp.utils;

import android.location.Location;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class LocationHelper {

    public static LocationRequest getLocationRequest() {
        // Define and configure your LocationRequest
        return new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000); // Set the interval for location updates (in milliseconds)
    }

    public static float calculateDistance(Location location1, LatLng location2) {
        float[] results = new float[1];
        Location.distanceBetween(
                location1.getLatitude(),
                location1.getLongitude(),
                location2.latitude,
                location2.longitude,
                results
        );
        return results[0];
    }
}

