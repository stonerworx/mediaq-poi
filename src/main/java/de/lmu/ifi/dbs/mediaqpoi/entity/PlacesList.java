package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.google.api.client.util.Key;

import java.util.List;

public class PlacesList {

  @Key
  public String status;

  @Key
  public List<Place> results;

}