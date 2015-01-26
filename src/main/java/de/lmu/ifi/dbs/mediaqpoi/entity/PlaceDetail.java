package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.google.api.client.util.Key;

public class PlaceDetail {

  @Key
  public Place result;

  @Override
  public String toString() {
    if (result!=null) {
      return result.toString();
    }
    return super.toString();
  }

}