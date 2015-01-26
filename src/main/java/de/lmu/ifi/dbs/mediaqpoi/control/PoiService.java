package de.lmu.ifi.dbs.mediaqpoi.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.time.StopWatch;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import de.lmu.ifi.dbs.mediaqpoi.boundary.IPoiService;
import de.lmu.ifi.dbs.mediaqpoi.control.dataimport.VideoImport;
import de.lmu.ifi.dbs.mediaqpoi.entity.AlgorithmApproachType;
import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Place;
import de.lmu.ifi.dbs.mediaqpoi.entity.PlacesList;
import de.lmu.ifi.dbs.mediaqpoi.entity.Poi;
import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

public class PoiService implements IPoiService {

  private static final Logger LOGGER = Logger.getLogger(PoiService.class.getName());
  private final static IPoiService instance = new PoiService();
  private static AlgorithmApproachType approach = AlgorithmApproachType.NAIVE;

  public static IPoiService getInstance() {
    return instance;
  }

  @Override
  public List<Video> getVideos(double longitude, double latitude) throws Exception {
    List<Video> result = null;
    StopWatch stopWatch = null;

    try {
      stopWatch = new StopWatch();
      stopWatch.start();
    } catch (Exception ignore) {
    }

    switch (approach) {
      case NAIVE:
        result = getVideosNaive(longitude, latitude);
        break;
      case GOOGLE_DOCUMENT_INDEX:
        result = PersistenceFacade.getVideos(longitude, latitude);
        break;
      case RTREE:
        result = VideoImport.rtree.getVideos(latitude, longitude);
        break;
      default:
    }

    try {
      stopWatch.stop();
      LOGGER.info(String.format("Got videos for location in %s milliseconds (using %s)", stopWatch.getTime(), approach.name()));
    } catch (Exception ignore) {
    }

    return result;
  }

  @Override
  public Map<Long, List<Poi>> getPois(Video video) throws Exception {
    String timeLineKey = "timeline_" + video.getKey();

    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

    Map<Long, List<Poi>> timeLine = (Map<Long, List<Poi>>) syncCache.get(timeLineKey);

    // recalculate if we don't have the timeline in cache.
    if (timeLine == null) {
      timeLine = new TreeMap<>();

      List<Poi> visiblePois = getVisiblePois(video);
      Trajectory trajectory = video.getTrajectory();

      for (Long timePosition : trajectory.getTimeLine().keySet()) {
        List<Poi> visiblePoisAtPosition = new ArrayList<>();
        for (Poi poi : visiblePois) {
          TrajectoryPoint point = trajectory.getTimeLine().get(timePosition);
          if (point.isVisible(poi.getLatitude(), poi.getLongitude())) {
            visiblePoisAtPosition.add(poi);
          }
        }
        timeLine.put(timePosition, visiblePoisAtPosition);
      }
      syncCache.put(timeLineKey, timeLine);
    }

    return timeLine;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Poi> getPoiCandidates(Video video) throws Exception {
    String key = "nearbyPois_" + video.getKey();

    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    List<Poi> nearbyPois = (ArrayList<Poi>) syncCache.get(key);
    if (nearbyPois == null) {
      nearbyPois = new ArrayList<>();

      Trajectory trajectory = video.getTrajectory();
      if (trajectory == null || trajectory.getTimeStampedPoints() == null) {
        LOGGER.warning("Video has no trajectory data");
        return null;
      }
      PlacesList places = GooglePlacesApi.searchPlaces(trajectory.getCenter(), trajectory.getSearchRange());

      for (Place place : places.results) {
        Poi poi = new Poi(place);
        nearbyPois.add(poi);
      }

      syncCache.put(key, nearbyPois);
    }

    LOGGER.info(String.format("Found %s Poi candidates for the given video", nearbyPois.size()));
    return nearbyPois;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Poi> getVisiblePois(Video video) throws Exception {
    String key = "visiblePois_" + video.getKey();

    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    List<Poi> visiblePois = (ArrayList<Poi>) syncCache.get(key);

    // recalculate if we don't have the visible POIs in cache.
    if (visiblePois == null) {

      List<Poi> candidates = getPoiCandidates(video);

      visiblePois = new ArrayList<>();
      Trajectory trajectory = video.getTrajectory();

      for (TrajectoryPoint trajectoryPoint : trajectory.getTimeStampedPoints()) {

        for (Poi poi : candidates) {
          if (visiblePois.contains(poi)) {
            // we already know that this Poi is visible, so we can skip further investigation
            continue;
          }

          if (trajectoryPoint.isVisible(poi.getLatitude(), poi.getLongitude())) {
            visiblePois.add(poi);
          }
        }
      }

      syncCache.put(key, visiblePois);
    }

    LOGGER.info(String.format("Found %s visible Pois for the given video", visiblePois.size()));
    return visiblePois;
  }

  @Override
  public Poi getPoi(String placeId) throws Exception {
    Place place = GooglePlacesApi.getDetails(placeId);
    if (place != null) {
      return new Poi(place);
    }
    return null;
  }

  @Override
  public List<Video> getVideosInRange(Location min, Location max) throws Exception {
    List<Video> result = null;
    StopWatch stopWatch = null;

    try {
      stopWatch = new StopWatch();
      stopWatch.start();
    } catch (Exception ignore) {
    }

    switch (approach) {
      case NAIVE:
        result = getVideosInRangeNaive(min, max);
        break;
      case GOOGLE_DOCUMENT_INDEX:
        result = PersistenceFacade.getVideosInRange(min, max);
        break;
      case RTREE:
        result = VideoImport.rtree.getVideosForArea(max, min);
        break;
      default:
    }

    try {
      stopWatch.stop();
      LOGGER.info(String.format("Got videos in range in %s milliseconds (using %s)", stopWatch.getTime(), approach.name()));
    } catch (Exception ignore) {
    }

    return result;
  }

  public static AlgorithmApproachType getApproach() {
    return approach;
  }

  public static void setApproach(AlgorithmApproachType approach) {
    PoiService.approach = approach;
  }

  /**
   * Naive approach for getting the videos that record a given geo location. All videos are
   * retrieved and iterated.
   */
  private List<Video> getVideosNaive(double longitude, double latitude) throws Exception {
    LOGGER.info("Performing video query for location with naive approach");
    try {
      List<Video> allVideos = PersistenceFacade.getVideos();
      List<Video> result = new CopyOnWriteArrayList<>();

      for (Video video : allVideos) {

        Trajectory trajectory = video.getTrajectory();
        if (trajectory == null || trajectory.getTimeStampedPoints() == null || result.contains(video)) {
          continue;
        }

        for (TrajectoryPoint point : trajectory.getTimeStampedPoints()) {
          if (point.isVisible(longitude, latitude)) {
            result.add(video);
          }
        }
      }
      LOGGER.info(String.format("Found %s videos for the given location (naive approach)", result.size()));
      return result;

    } catch (Exception e) {
      LOGGER.severe("Exception occurred in naive approach of getting all videos recording a specific geo location: " + e);
      throw e;
    }
  }

  /**
   * Naive approach for getting the videos in a given range area. All videos are retrieved and
   * iterated.
   */
  private List<Video> getVideosInRangeNaive(Location min, Location max) throws Exception {
    LOGGER.info("Performing range query with naive approach");
    try {
      List<Video> allVideos = PersistenceFacade.getVideos();
      List<Video> result = new CopyOnWriteArrayList<>();

      for (Video video : allVideos) {

        Trajectory trajectory = video.getTrajectory();
        if (GeoHelper.isInRange(trajectory, min, max)) {
          result.add(video);
        }
      }
      LOGGER.info(String.format("Found %s videos in range (naive approach)", result.size()));
      return result;

    } catch (Exception e) {
      LOGGER.severe("Exception occurred in naive approach of getting all videos in a given range area: " + e);
      throw e;
    }
  }

}
