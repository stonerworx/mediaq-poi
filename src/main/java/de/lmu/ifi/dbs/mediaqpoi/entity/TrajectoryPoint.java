package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.io.Serializable;

@SuppressWarnings("serial")
@PersistenceCapable
public class TrajectoryPoint implements Serializable, Comparable<TrajectoryPoint> {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private int frame;

  @Persistent
  private double latitude;

  @Persistent
  private double longitude;

  @Persistent
  private double thetaX;

  @Persistent
  private double thetaY;

  @Persistent
  private double thetaZ;

  @Persistent
  private double r;

  @Persistent
  private int alpha;

  @Persistent
  private long timeStamp;

  public TrajectoryPoint(int frame, double latitude, double longitude, double thetaX, double thetaY, double thetaZ, double r, int alpha, long timeStamp) {
    this.frame = frame;
    this.latitude = latitude;
    this.longitude = longitude;
    this.thetaX = thetaX;
    this.thetaY = thetaY;
    this.thetaZ = thetaZ;
    this.r = r;
    this.alpha = alpha;
    this.timeStamp = timeStamp;
  }

  public int getFrame() {
    return frame;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getThetaX() {
    return thetaX;
  }

  public double getThetaY() {
    return thetaY;
  }

  public double getThetaZ() {
    return thetaZ;
  }

  public double getR() {
    return r;
  }

  public int getAlpha() {
    return alpha;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  @Override
  public int compareTo(TrajectoryPoint o) {
    if (o.getTimeStamp() > getTimeStamp()) {
      return -1;
    } else if (o.getTimeStamp() == getTimeStamp()) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public String toString() {
    return "frame: " + frame + ", latitude: " + latitude + ", longitude: " + longitude + ", thetaX: " + thetaX + ", thetaY: " + thetaY + ", thetaZ: " + thetaZ + ", timeCode: " + timeStamp;
  }

  /**
   * calculates if a given location is visible from this trajectory point.
   * 
   * @param latitude of the location
   * @param longitude of the location
   * @return true if visible else false
   */
  public boolean isVisible(double latitude, double longitude) {

    double distance = Distance.getDistanceInMeters(new Location(this.latitude, this.longitude), new Location(latitude, longitude));

    if (distance > Distance.VISIBILITY_RANGE) {
      return false;
    } else {

      double minAngle = this.thetaX - (double) this.alpha / 2d;
      double maxAngle = this.thetaX + (double) this.alpha / 2d;
      double angle = this.getAngleTo(new Location(latitude, longitude));

      // normalization
      double factor = minAngle;
      minAngle = minAngle - factor;
      maxAngle = maxAngle - factor;
      angle = (angle - factor) % 360;
      if (angle < 0) {
        angle = angle + 360;
      }

        return minAngle < angle && angle < maxAngle;

    }

  }

  /**
   * calculates the angle between this trajectory point and a given location.
   * 
   * @param loc
   * @return angle in degree
   */
  private double getAngleTo(Location loc) {

    double dLon = (loc.longitude - this.longitude);

    double y = Math.sin(dLon) * Math.cos(loc.latitude);
    double x = Math.cos(this.latitude) * Math.sin(loc.latitude) - Math.sin(this.latitude) * Math.cos(loc.latitude) * Math.cos(dLon);

    double brng = Math.atan2(y, x);

    brng = Math.toDegrees(brng);
    brng = (brng + 360) % 360;
    brng = 360 - brng;

    return brng;

  }

}
