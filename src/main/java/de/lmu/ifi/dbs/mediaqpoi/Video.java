package de.lmu.ifi.dbs.mediaqpoi;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Video {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private String title;

  @Persistent
  private String id;

  @Persistent
  @SerializedName("trajectory")
  private List<TrajectoryPoint> trajectory = new ArrayList<TrajectoryPoint>();

  @NotPersistent
  private SearchRadius searchRadius;

  @NotPersistent
  private List<Poi> nearbyPois = new ArrayList<Poi>();

  @NotPersistent
  private List<Poi> visiblePois = new ArrayList<Poi>();

  @NotPersistent
  private Map<Integer, Map<String, List>> timeline = new HashMap<Integer, Map<String, List>>();

  public Video(String id, String title) {
    this.id = id;
    this.title = title;
    Key key = KeyFactory.createKey(Video.class.getSimpleName(), id);
    setKey(key);
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public Key getKey() {
    return key;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public List<TrajectoryPoint> getTrajectory() {
    return trajectory;
  }

  public SearchRadius getSearchRadius() {
    //TODO: calculate center and radius that surrounds trajectory

    SearchRadius searchRadius = new SearchRadius(48.150529, 11.595077, 500);
    this.searchRadius = searchRadius;

    return searchRadius;
  }

  public List<Poi> getNearbyPois() {
    String key = "nearbyPois_" + this.getKey();

    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    nearbyPois = (ArrayList<Poi>) syncCache.get(key);
    if (nearbyPois == null) {
      nearbyPois = new ArrayList<Poi>();

      PlacesList places = GooglePlacesApi.searchPlaces(getSearchRadius());

      for (Place place : places.results) {
        Poi poi = new Poi(place.id, place.reference, place.geometry.location.lat,
                          place.geometry.location.lng, place.name);
        nearbyPois.add(poi);
      }

      syncCache.put(key, nearbyPois);
    }

    return nearbyPois;
  }

  public List<Poi> getVisiblePois() {
    List<Poi> pois = getNearbyPois();

    String key = "visiblePois_" + this.getKey();

    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    visiblePois = (ArrayList<Poi>) syncCache.get(key);

    String timelineKey = "timeline_" + this.getKey();
    timeline = (HashMap<Integer, Map<String, List>>) syncCache.get(timelineKey);

    //recalculate if we don't have the visible POIs and timeline in cache.
    if (visiblePois == null || timeline == null) {

      visiblePois = new ArrayList<Poi>();
      timeline = new HashMap<Integer, Map<String, List>>();

      for (TrajectoryPoint trajectoryPoint : trajectory) {

        //at what position in the video are we?
        long startTime = trajectory.get(0).getTimecode();
        long currentTime = trajectoryPoint.getTimecode();
        //position of this TrajectoryPoint in the video in seconds.
        int position = Math.round((currentTime - startTime) / 1000);

        Map<String, List> timelineElement;
        if (timeline.containsKey(position)) {
          timelineElement = timeline.get(position);
        } else {
          timelineElement = new HashMap<String, List>();
        }

        //add the current frame number to the timeline
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

        //TODO: calculate which POIs are visible right now. - ONLY DEMO DATA AT THE MOMENT.
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

  public void addTrajectoryPoint(TrajectoryPoint trajectoryPoint) {
    trajectory.add(trajectoryPoint);
  }
}
