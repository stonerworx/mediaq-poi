package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.google.api.client.util.Key;
import com.google.appengine.api.search.GeoPoint;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;

public class Location {

  @Key("lat")
  @Expose
  public double latitude;

  @Key("lng")
  @Expose
  public double longitude;

  public Location() {
  }

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

  public String toString() {
    return "(" + latitude + ", " + longitude + ")";
  }

  public GeoPoint toGeoPoint() {
    return new GeoPoint(latitude, longitude);
  }

  public LatLonPoint toLatLonPoint() {
    return new LatLonPointImpl(latitude, longitude);
  }
}
