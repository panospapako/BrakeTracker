package com.unipi.ppapakostas.braketracker.model;

public class BrakingPoint {

    private final double latitude;
    private final double longitude;
    private final String timestamp;
    private final float acceleration;

    public BrakingPoint(double latitude, double longitude, String timestamp, float acceleration) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.acceleration = acceleration;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getAcceleration() { return acceleration; }

}
