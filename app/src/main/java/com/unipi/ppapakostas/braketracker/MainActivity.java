package com.unipi.ppapakostas.braketracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Main activity that handles tracking the car's speed and detecting braking events.
 * It also allows the user to navigate to Google Maps or a RecyclerView of braking points.
 */
public class MainActivity extends AppCompatActivity {

    // UI Elements
    private RelativeLayout RLStartTracking, RL_RLCurrentSpeed;
    private TextView tvCurrentSpeed;

    // Location and tracking elements
    private FusedLocationProviderClient fusedLocationProviderClient;
    public static final int LOCATION_CODE = 23;
    public static final int REQUEST_CODE = 10;

    // Database instance
    private Database database;

    // Variables to store location and speed details
    private Double lon, lat;
    private String timeStamp;
    private float acceleration;
    private float lastSpeed = 0f;
    private long lastTime = 0;
    private boolean isTracking = false;
    private Animation pulseAnimation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        database = Database.getInstance(this);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_maps) {
                    openGoogleMaps();
                    return true;
                } else if (id == R.id.nav_recyclerview) {
                    openRecyclerView();
                    return true;
                }
                return false;
            }
        });

        RLStartTracking = findViewById(R.id.RLStartTracking);
        RL_RLCurrentSpeed= findViewById(R.id.RL_RLCurrentSpeed);
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);
        tvCurrentSpeed = findViewById(R.id.tvCurrentSpeed);

        RLStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTracking) {
                    startTracking();
                } else {
                    stopTracking();
                }
                isTracking = !isTracking;
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        askPermission();
    }

    LocationRequest locationRequest = new LocationRequest.Builder(5000)
            .setMinUpdateIntervalMillis(2000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build();

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            RL_RLCurrentSpeed.setVisibility(TextView.VISIBLE);
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                float currentSpeed = location.getSpeed(); // speed in m/s
                int roundedSpeed = (int) Math.round(currentSpeed * 3.6); //speed in km/h
                long currentTime = System.currentTimeMillis();

                if (lastTime != 0) {
                    float speedDifference = currentSpeed - lastSpeed;
                    long timeDifference = currentTime - lastTime; // in milliseconds
                    float timeDifferenceSeconds = timeDifference / 1000f;
                    acceleration = speedDifference / timeDifferenceSeconds;
                    Log.d("Speed: ", String.valueOf(currentSpeed));
                    Log.d("Acceleration: ", String.valueOf(acceleration));
                    tvCurrentSpeed.setText("Speed: " + roundedSpeed + " km/h");

                    // Detect braking when acceleration is below -2 m/s²
                    if (acceleration < -2) {
                        Log.d("Braking", "Braking detected with acceleration: " + acceleration);
                        lon = location.getLongitude();
                        Log.d("Lon: ", String.valueOf(lon));
                        lat = location.getLatitude();
                        Log.d("Lat: ", String.valueOf(lat));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date date = new Date(System.currentTimeMillis());
                        timeStamp = simpleDateFormat.format(date);
                        Log.d("Time: ", timeStamp);
                        Log.d("Acceleration: ", String.valueOf(acceleration));
                        long id = database.insert(lon, lat, timeStamp, acceleration);
                        if(id == -1){
                            Log.e("ERROR INSERT TO DB", "id: "+id);
                            Toast.makeText(MainActivity.this, "Failed to save braking point. Please try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        setNotification(lon, lat, timeStamp);
                    }
                }

                lastSpeed = currentSpeed;
                lastTime = currentTime;
            }
        }
    };


    /**
     * Sets a notification to alert the user when braking is detected.
     *
     * @param lon       The longitude of the braking point
     * @param lat       The latitude of the braking point
     * @param timestamp The timestamp of the braking event
     */
    @SuppressLint("ScheduleExactAlarm")
    private void setNotification(Double lon, Double lat, String timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 3);

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("Lon", lon);
        intent.putExtra("Lat", lat);
        intent.putExtra("Time", timestamp);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /**
     * Starts tracking the user's location and speed.
     */
    private void startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askPermission();  // Ask for permission if not already granted
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        RLStartTracking.startAnimation(pulseAnimation);
        RLStartTracking.setBackground(ContextCompat.getDrawable(this, R.drawable.tracking_background));
        Toast.makeText(this, "Tracking started", Toast.LENGTH_SHORT).show();
    }

    /**
     * Stops tracking the user's location and speed.
     */
    private void stopTracking() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            RL_RLCurrentSpeed.setVisibility(TextView.INVISIBLE);
            RLStartTracking.clearAnimation();
            RLStartTracking.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_main_button));
            Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Opens the Google Maps activity to view braking points on a map.
     */
    private void openGoogleMaps(){
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    /**
     * Opens the RecyclerView activity to view recorded braking points.
     */
    private void openRecyclerView() {
        Intent intent = new Intent(MainActivity.this, RecyclerViewActivity.class);
        startActivity(intent);
    }

    /**
     * Handles the result of permission requests.
     *
     * @param requestCode  The request code passed in requestPermissions()
     * @param permissions  The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isTracking) {
                    startTracking();
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Requests the necessary location permissions from the user.
     */
    public void askPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_CODE);
        }
    }
}