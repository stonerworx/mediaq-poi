package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Poi implements Serializable {

  @Expose
  private String reference;
  @Expose
  private String id;
  @Expose
  private double latitude;
  @Expose
  private double longitude;
  @Expose
  private String name;

  public Poi(String id, String reference, double latitude, double longitude, String name) {
    this.id = id;
    this.reference = reference;
    this.latitude = latitude;
    this.longitude = longitude;
    this.name = name;
  }

  public Poi(Place place) {
    this(place.placeId, place.reference, place.geometry.location.getLatitude(),
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
