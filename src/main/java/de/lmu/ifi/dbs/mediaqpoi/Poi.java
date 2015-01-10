package de.lmu.ifi.dbs.mediaqpoi;

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

  @Override
  public String toString() {
    return this.name + " (" + this.latitude + ", " + this.longitude + ")";
  }
}
