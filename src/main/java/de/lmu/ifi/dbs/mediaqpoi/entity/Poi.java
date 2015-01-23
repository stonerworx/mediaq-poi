package de.lmu.ifi.dbs.mediaqpoi.entity;

import java.io.Serializable;

public class Poi implements Serializable {

  private String reference;
  private String id;
  private double latitude;
  private double longitude;
  private String name;

  public Poi(String id, String reference, double latitude, double longitude, String name) {
    this.id = id;
    this.reference = reference;
    this.latitude = latitude;
    this.longitude = longitude;
    this.name = name;
  }

  public Poi(Place place) {
    this(place.id, place.reference, place.geometry.location.getLatitude(),
        place.geometry.location.getLongitude(), place.name);
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  @Override
  public String toString() {
    return this.name + " (" + this.latitude + ", " + this.longitude + ")";
  }

}
