package de.lmu.ifi.dbs.mediaqpoi.control;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import de.lmu.ifi.dbs.mediaqpoi.control.dataimport.DumpFileParser;
import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class VideoRTree {

  private static final Logger LOGGER = Logger.getLogger(VideoRTree.class.getName());
  private static final double MIN_LAT = Math.toRadians(-90d);
  private static final double MAX_LAT = Math.toRadians(90d);
  private static final double MIN_LON = Math.toRadians(-180d);
  private static final double MAX_LON = Math.toRadians(180d);
  private static final VideoRTree instance = new VideoRTree();
  private static int ID = 0;
  private SpatialIndex spatialIndex;
  private Map<Integer, Video> map;

  public VideoRTree() {
    spatialIndex = new RTree();
    spatialIndex.init(null);
    map = new HashMap<>();
  }

  public static VideoRTree getInstance() {
    return instance;
  }

  private static double deg2rad(double deg) {
    return deg * (Math.PI / 180);
  }

  private static double rad2deg(double rad) {
    return (rad * 180 / Math.PI);
  }

  /**
   * just for testing
   *
   * @param args
   */
  public static void main(String[] args) {

    try {

      DumpFileParser dumpFileParser = new DumpFileParser();
      dumpFileParser.parse("video_info.sql");
      dumpFileParser.parse("video_metadata.sql");
      List<Video> videos = dumpFileParser.getVideos();
      System.out.println("videos.size() = " + videos.size());

      VideoRTree videoRTree = new VideoRTree();
      videoRTree.insertAll(videos);

      List<Video> candidates = videoRTree.getCandidates(48.152588, 11.590518);
      System.out.println("candidates:");
      for (Video video : candidates) {
        System.out.println(video.getFileName() + " --> " + video.getTrajectory().getMaxLocation() + video.getTrajectory().getMinLocation());
      }

      Location maxLocation = new Location(6.683091, 10.724728);
      Location minLocation = new Location(-6.296802, -9.490116);
      List<Video> videosInArea = videoRTree.getVideosForArea(maxLocation, minLocation);
      System.out.println("videos in area:");
      for (Video video : videosInArea) {
        System.out.println(video.getFileName() + " --> " + video.getTrajectory().getMaxLocation() + video.getTrajectory().getMinLocation());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }


  }

  /**
   * Inserts the video into this R-tree.
   *
   * @param video
   */
  public void insert(Video video) {
    // System.out.print("insert " + video.getFileName());
    Trajectory trajectory = video.getTrajectory();
    if (trajectory != null) {
      Location maxLocation = trajectory.getMaxLocation();
      Location minLocation = trajectory.getMinLocation();
      Rectangle mbr = new Rectangle((float) maxLocation.latitude, (float) maxLocation.longitude, (float) minLocation.latitude, (float) minLocation.longitude);
      spatialIndex.add(mbr, ID);
      map.put(ID, video);
      ID++;
      // System.out.println(" --> done");
    } else {
      // System.out.println(" --> trajectory == null");
    }
  }

  /**
   * Inserts all videos into this R-tree.
   *
   * @param videos
   */
  public void insertAll(List<Video> videos) {
    for (Video video : videos) {
      insert(video);
    }
  }

  /**
   * calculates all videos within the given area.
   *
   * @param max maxLocation of the area
   * @param min minLocation of the area
   * @return list of videos within the area
   */
  public List<Video> getVideosForArea(Location max, Location min) {
    float maxLat = (float) max.latitude;
    float maxLon = (float) max.longitude;
    float minLat = (float) min.latitude;
    float minLon = (float) min.longitude;
    SaveToListProcedure saveToListProcedure = new SaveToListProcedure();
    spatialIndex.intersects(new Rectangle(maxLat, maxLon, minLat, minLon), saveToListProcedure);
    List<Video> result = saveToListProcedure.getVideos();
    LOGGER.info(String.format("Found %s videos for range in the r tree", result.size()));
    return result;
  }

  /**
   * calculates the video candidates for a given poi location.
   *
   * @param latitude of poi
   * @param longitude of poi
   * @return list of video candidates
   */
  public List<Video> getCandidates(double latitude, double longitude) {
    Location location = new Location(latitude, longitude);
    double distance = (double) GeoHelper.VISIBILITY_RANGE / 1000;
    BoundingCoordinates boundingCoordinates = getBoundingCoordinates(location, distance);
    Location maxLocation = boundingCoordinates.getMaxLocation();
    Location minLocation = boundingCoordinates.getMinLocation();
    float maxLat = (float) maxLocation.latitude;
    float maxLon = (float) maxLocation.longitude;
    float minLat = (float) minLocation.latitude;
    float minLon = (float) minLocation.longitude;
    SaveToListProcedure saveToListProcedure = new SaveToListProcedure();
    spatialIndex.intersects(new Rectangle(maxLat, maxLon, minLat, minLon), saveToListProcedure);
    List<Video> result = saveToListProcedure.getVideos();
    LOGGER.info(String.format("Found %s candidates for geo location in the r tree", result.size()));
    return result;
  }

  /**
   * calculates all videos that record a given poi location.
   *
   * @param latitude of poi
   * @param longitude of poi
   * @return video result list
   */
  public List<Video> getVideos(double latitude, double longitude) {
    List<Video> candidates = getCandidates(latitude, longitude);
    List<Video> result = new ArrayList<>();
    for (Video video : candidates) {
      Trajectory trajectory = video.getTrajectory();
      if (trajectory != null && trajectory.getTimeStampedPoints() != null) {
        for (TrajectoryPoint point : trajectory.getTimeStampedPoints()) {
          if (point.isVisible(latitude, longitude)) {
            result.add(video);
            break;
          }
        }
      }
    }
    LOGGER.info(String.format("Found %s videos for geo location in the r tree", result.size()));
    return result;
  }

  private BoundingCoordinates getBoundingCoordinates(Location location, double distance) {

    if (distance < 0d)
      throw new IllegalArgumentException();

    // radius of the earth in km
    double radius = 6371;
    // angular distance in radians
    double radDist = distance / radius;

    double minLat = deg2rad(location.latitude) - radDist;
    double maxLat = deg2rad(location.latitude) + radDist;
    double minLon;
    double maxLon;

    if (minLat > MIN_LAT && maxLat < MAX_LAT) {
      double deltaLon = Math.asin(Math.sin(radDist) / Math.cos(deg2rad(location.latitude)));
      minLon = deg2rad(location.longitude) - deltaLon;
      if (minLon < MIN_LON)
        minLon += 2d * Math.PI;
      maxLon = deg2rad(location.longitude) + deltaLon;
      if (maxLon > MAX_LON)
        maxLon -= 2d * Math.PI;
    } else {
      // a pole is within the distance
      minLat = Math.max(minLat, MIN_LAT);
      maxLat = Math.min(maxLat, MAX_LAT);
      minLon = MIN_LON;
      maxLon = MAX_LON;
    }

    Location maxLocation = new Location(rad2deg(maxLat), rad2deg(maxLon));
    Location minLocation = new Location(rad2deg(minLat), rad2deg(minLon));

    return new BoundingCoordinates(maxLocation, minLocation);
  }


  class BoundingCoordinates {

    Location maxLocation;
    Location minLocation;

    public BoundingCoordinates(Location maxLocation, Location minLocation) {
      this.maxLocation = maxLocation;
      this.minLocation = minLocation;
    }

    public Location getMaxLocation() {
      return maxLocation;
    }

    public Location getMinLocation() {
      return minLocation;
    }

  }


  class SaveToListProcedure implements TIntProcedure {

    private List<Video> videos = new CopyOnWriteArrayList<>();

    public boolean execute(int id) {
      videos.add(map.get(id));
      return true;
    }

    private List<Video> getVideos() {
      return videos;
    }

  }

}
