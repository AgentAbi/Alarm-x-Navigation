package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.myapp.utils.LocationHelper;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LatLng destinationLatLng;
    private EditText destinationEditText;
    private static final String CHANNEL_ID = "DestinationAlertChannel";

    @Override


        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        destinationEditText = findViewById(R.id.destinationEditText);
        Button setDestinationButton = findViewById(R.id.setDestinationButton);

        setDestinationButton.setOnClickListener(view -> setDestination());

        createNotificationChannel();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Check the distance and trigger the alarm if needed
                    checkDistanceAndTriggerAlarm(location);
                }
            }
        };
    }

    private void createNotificationChannel() {
        // Check if Android Oreo or higher, and create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Destination Alert", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void setDestination() {
        String destination = destinationEditText.getText().toString();
        if (!destination.isEmpty()) {
            // Use Geocoding to convert the destination string to LatLng
            // This step is not implemented in this basic example
            // You may need to use the Places API or other services to convert the destination string to LatLng

            // For demonstration purposes, let's assume the destination is hardcoded
            destinationLatLng = new LatLng(37.7749, -122.4194);

            // Update the map and zoom to the destination
            if (googleMap != null) {
                googleMap.clear(); // Clear previous markers

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(destinationLatLng)
                        .title("Destination");
                googleMap.addMarker(markerOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15));

                // Start continuous monitoring of the user's location
                startLocationUpdates();
            }
        } else {
            Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {
        // Check and request location permissions if needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(LocationHelper.getLocationRequest(), locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void checkDistanceAndTriggerAlarm(Location currentLocation) {
        if (destinationLatLng != null) {
            // Calculate distance between current location and destination
            float distance = LocationHelper.calculateDistance(currentLocation, destinationLatLng);

            // Check if the user is 2km away from the destination
            if (distance >= 2000) {
                // Trigger the alarm (show notification)
                sendNotification();

                // Stop location updates if needed
                stopLocationUpdates();
            }
        }
    }

    private void sendNotification() {
        // Create an explicit intent for an activity in your app
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Destination Alert")
                .setContentText("You are 2km away from your destination.")
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        // Get the notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Show the notification
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }



    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Check and request location permissions if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        // Check if Google Play Services is available
        if (googleMap != null) {
            // Enable the user's location on the map
            googleMap.setMyLocationEnabled(true);

            // You can further customize the map settings based on your requirements
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, for further development, do any location-related operations here
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates(); // Stop location updates when the activity is destroyed
    }
}

