package de.lmu.ifi.dbs.mediaqpoi.entity;

import java.util.TreeSet;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Trajectory {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private com.google.appengine.api.datastore.Key key;

  @Persistent
  private TreeSet<TrajectoryPoint> timeStampedPoints;

  public TreeSet<TrajectoryPoint> getTimeStampedPoints() {
    return timeStampedPoints;
  }

  public void setTimeStampedPoints(TreeSet<TrajectoryPoint> timeStampedPoints) {
    this.timeStampedPoints = timeStampedPoints;
  }

  public void add(TrajectoryPoint point) {
    if (timeStampedPoints != null) {
      timeStampedPoints.add(point);
    }
  }

  public Location calculateCenter() {
    double maxLat = 90;
    double minLat = -90;
    double maxLng = 180;
    double minLng = -180;
    for (TrajectoryPoint point : getTimeStampedPoints()) {
      if (point.getLatitude() < minLat) {
        minLat = point.getLatitude();
      }
      if (point.getLatitude() > maxLat) {
        maxLat = point.getLatitude();
      }
      if (point.getLongitude() < minLng) {
        minLng = point.getLongitude();
      }
      if (point.getLongitude() > maxLng) {
        maxLng = point.getLongitude();
      }
    }
    double lat = (maxLat + minLat) / 2;
    double lng = (maxLng + minLng) / 2;
    return new Location(lat, lng);
  }

  public long calculateSearchRange() {
    double maxLat = 90;
    double minLat = -90;
    double maxLng = 180;
    double minLng = -180;
    for (TrajectoryPoint point : getTimeStampedPoints()) {
      if (point.getLatitude() < minLat) {
        minLat = point.getLatitude();
      }
      if (point.getLatitude() > maxLat) {
        maxLat = point.getLatitude();
      }
      if (point.getLongitude() < minLng) {
        minLng = point.getLongitude();
      }
      if (point.getLongitude() > maxLng) {
        maxLng = point.getLongitude();
      }
    }
    Location maxLocation = new Location(maxLat, maxLng);
    Location minLocation = new Location(minLat, minLng);
    long radius = (long) Distance.getDistanceInMeters(maxLocation, minLocation) / 2 + Distance.VISIBILITY_RANGE;
    return radius;
  }

  public com.google.appengine.api.datastore.Key getKey() {
    return key;
  }

  public void setKey(com.google.appengine.api.datastore.Key key) {
    this.key = key;
  }

  public long getStartTime() {
    if (timeStampedPoints != null) {
      return ((TreeSet<TrajectoryPoint>) timeStampedPoints).first().getTimecode();
    } else {
      return -1;
    }
  }

  public long getEndTime() {
    if (timeStampedPoints != null) {
      return ((TreeSet<TrajectoryPoint>) timeStampedPoints).last().getTimecode();
    } else {
      return -1;
    }
  }

  public TrajectoryPoint getStartPoint() {
    if (timeStampedPoints != null) {
      return ((TreeSet<TrajectoryPoint>) timeStampedPoints).first();
    } else {
      return null;
    }
  }

  public TrajectoryPoint getEndPoint() {
    if (timeStampedPoints != null) {
      return ((TreeSet<TrajectoryPoint>) timeStampedPoints).last();
    } else {
      return null;
    }
  }

}
