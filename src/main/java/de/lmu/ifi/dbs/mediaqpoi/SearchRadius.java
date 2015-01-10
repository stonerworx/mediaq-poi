package de.lmu.ifi.dbs.mediaqpoi;

public class SearchRadius {

  private double latitude;
  private double longitude;
  private int radius;

  public SearchRadius(double latitude, double longitude, int radius) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.radius = radius;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public int getRadius() {
    return radius;
  }
}
