package de.lmu.ifi.dbs.mediaqpoi.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import de.lmu.ifi.dbs.mediaqpoi.boundary.IPoiService;
import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Place;
import de.lmu.ifi.dbs.mediaqpoi.entity.PlacesList;
import de.lmu.ifi.dbs.mediaqpoi.entity.Poi;
import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

public class PoiService implements IPoiService {

  private static final Logger LOGGER = Logger.getLogger(PoiService.class.getName());
  private final static PoiService instance = new PoiService();

  public static PoiService getInstance() {
    return instance;
  }

  @Override
  public List<Video> getVideos(long longitude, long latitude) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<Long, List<Poi>> getPois(Video video) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Poi> getPoiCandidates(Video video) throws Exception {
    String key = "nearbyPois_" + video.getKey();

    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    List<Poi> nearbyPois = (ArrayList<Poi>) syncCache.get(key);
    if (nearbyPois == null) {
      nearbyPois = new ArrayList<Poi>();

      PlacesList places =
          GooglePlacesApi.searchPlaces(video.getTrajectory().calculateCenter(), video
              .getTrajectory().calculateSearchRange());

      for (Place place : places.results) {
        Poi poi =
            new Poi(place.id, place.reference, place.geometry.location.getLatitude(),
                place.geometry.location.getLongitude(), place.name);
        nearbyPois.add(poi);
      }

      syncCache.put(key, nearbyPois);
    }

    return nearbyPois;
  }

  @SuppressWarnings("unchecked")
  public List<Poi> getVisiblePois(Video video) throws Exception {
    List<Poi> pois = getPoiCandidates(video);

    String key = "visiblePois_" + video.getKey();

    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    List<Poi> visiblePois = (ArrayList<Poi>) syncCache.get(key);

    String timelineKey = "timeline_" + video.getKey();
    Map<Integer, Map<String, List>> timeline =
        (HashMap<Integer, Map<String, List>>) syncCache.get(timelineKey);

    // recalculate if we don't have the visible POIs and timeline in cache.
    if (visiblePois == null || timeline == null) {

      visiblePois = new ArrayList<Poi>();
      timeline = new HashMap<Integer, Map<String, List>>();
      Trajectory trajectory = video.getTrajectory();

      for (TrajectoryPoint trajectoryPoint : trajectory.getTimeStampedPoints()) {

        // at what position in the video are we?
        long startTime = trajectory.getStartTime();
        long currentTime = trajectoryPoint.getTimecode();
        // position of this TrajectoryPoint in the video in seconds.
        int position = Math.round((currentTime - startTime) / 1000);

        Map<String, List> timelineElement;
        if (timeline.containsKey(position)) {
          timelineElement = timeline.get(position);
        } else {
          timelineElement = new HashMap<String, List>();
        }

        // add the current frame number to the timeline
        List<Integer> frames;
        if (timelineElement.containsKey("frames")) {
          frames = timelineElement.get("frames");
        } else {
          frames = new ArrayList<Integer>();
        }
        frames.add(trajectoryPoint.getFrame());

        timelineElement.put("frames", frames);

        List<Poi> visible;
        if (timelineElement.containsKey("pois")) {
          visible = timelineElement.get("pois");
        } else {
          visible = new ArrayList<Poi>();
        }

        // TODO: calculate which POIs are visible right now. - ONLY DEMO
        // DATA AT THE MOMENT.
        // --- DEMO DATA
        if (trajectoryPoint.getFrame() > 20 && trajectoryPoint.getFrame() < 50) {
          Poi poi = pois.get(0);
          if (!visible.contains(poi)) {
            visible.add(poi);
          }
          if (!visiblePois.contains(poi)) {
            visiblePois.add(poi);
          }
        }
        if (trajectoryPoint.getFrame() > 30 && trajectoryPoint.getFrame() < 40) {
          Poi poi = pois.get(1);
          if (!visible.contains(poi)) {
            visible.add(poi);
          }
          if (!visiblePois.contains(poi)) {
            visiblePois.add(poi);
          }
        }
        // ---

        timelineElement.put("pois", visible);

        timeline.put(position, timelineElement);
      }

      syncCache.put(key, visiblePois);
      syncCache.put(timelineKey, timeline);

    }

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
    // TODO Auto-generated method stub
    return null;
  }
}
