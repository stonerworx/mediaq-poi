package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.google.api.client.util.Key;

public class Location {

  @Key("lat")
  public double latitude;

  @Key("lng")
  public double longitude;

  public Location() {
  }

  ;

  public Location(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }
}