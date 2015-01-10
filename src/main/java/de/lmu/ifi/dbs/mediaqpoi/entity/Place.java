package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.google.api.client.util.Key;

public class Place {

  @Key
  public String id;

  @Key
  public String name;

  @Key
  public String reference;

  @Key
  public Geometry geometry;

  @Override
  public String toString() {
    return name + " - " + id + " - " + reference;
  }

}