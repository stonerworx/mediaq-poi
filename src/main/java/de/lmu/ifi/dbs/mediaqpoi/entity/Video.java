package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.annotations.Expose;

import javax.jdo.annotations.*;

@PersistenceCapable
public class Video {

  private static final String
      URL_VIDEO =
      "http://mediaq.dbs.ifi.lmu.de/MediaQ_MVC_V2/video_content/";

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  @Expose
  private String fileName;

  @Persistent(dependent = "true")
  private Trajectory trajectory;

  @NotPersistent
  @Expose
  private String filePath;

  @NotPersistent
  @Expose
  private String id;

  @NotPersistent
  @Expose
  private double latitude;

  @NotPersistent
  @Expose
  private double longitude;

  public Video(String fileName) {
    this.fileName = fileName;
    setFilePath(fileName);
    Key key = KeyFactory.createKey(Video.class.getSimpleName(), fileName);
    setKey(key);
  }

  public Video() {
    super();
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String name) {
    this.fileName = name;
  }

  public Trajectory getTrajectory() {
    return trajectory;
  }

  public void setTrajectory(Trajectory trajectory) {
    this.trajectory = trajectory;
  }

  public void setFilePath(String fileName)
  {
    filePath = URL_VIDEO + fileName;
  }

  public String getFilePath() {
    return filePath;
  }

  public void touch()
  {
    id = getKey().getName();
    setFilePath(fileName);
    TrajectoryPoint first = getTrajectory().getTimeStampedPoints().first();
    latitude = first.getLatitude();
    longitude = first.getLongitude();
  }

}
