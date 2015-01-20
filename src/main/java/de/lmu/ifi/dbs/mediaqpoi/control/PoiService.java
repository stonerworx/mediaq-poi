package de.lmu.ifi.dbs.mediaqpoi.control;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import de.lmu.ifi.dbs.mediaqpoi.boundary.IPoiService;
import de.lmu.ifi.dbs.mediaqpoi.entity.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PoiService implements IPoiService {

    private static final Logger LOGGER = Logger.getLogger(PoiService.class.getName());
    private static final String TIMELINE_KEY_FRAMES = "frames";
    private static final String TIMELINE_KEY_POIS = "pois";
    private final static PoiService instance = new PoiService();

    public static PoiService getInstance() {
        return instance;
    }

    @Override public List<Video> getVideos(long longitude, long latitude) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public Map<Long, List<Poi>> getPois(Video video) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked") @Override public List<Poi> getPoiCandidates(Video video)
        throws Exception {
        String key = "nearbyPois_" + video.getKey();

        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        List<Poi> nearbyPois = (ArrayList<Poi>) syncCache.get(key);
        if (nearbyPois == null) {
            nearbyPois = new ArrayList<Poi>();

            PlacesList places = GooglePlacesApi
                .searchPlaces(video.getTrajectory().calculateCenter(),
                    video.getTrajectory().calculateSearchRange());

            for (Place place : places.results) {
                Poi poi = new Poi(place);
                nearbyPois.add(poi);
            }

            syncCache.put(key, nearbyPois);
        }

        return nearbyPois;
    }

    @SuppressWarnings("unchecked") @Override public List<Poi> getVisiblePois(Video video)
        throws Exception {
        String key = "visiblePois_" + video.getKey();

        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        List<Poi> visiblePois = (ArrayList<Poi>) syncCache.get(key);

        // recalculate if we don't have the visible POIs in cache.
        if (visiblePois == null) {

            List<Poi> candidates = getPoiCandidates(video);

            visiblePois = new ArrayList<Poi>();
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

        return visiblePois;
    }

    @SuppressWarnings("unchecked")
    /**
     * returns a timeline, sorted by second in video, with a list of pois visible at this time
     */ public TreeMap<Integer, Map<String, List>> getTimeline(Video video) throws Exception {

        String timelineKey = "timeline_" + video.getKey();

        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

        TreeMap<Integer, Map<String, List>> timeline =
            (TreeMap<Integer, Map<String, List>>) syncCache.get(timelineKey);

        // recalculate if we don't have the timeline in cache.
        if (timeline == null) {

            List<Poi> visiblePois = getVisiblePois(video);

            timeline = new TreeMap<Integer, Map<String, List>>();
            Trajectory trajectory = video.getTrajectory();

            // at what position in the video are we?
            long startTime = trajectory.getStartTime();

            for (TrajectoryPoint trajectoryPoint : trajectory.getTimeStampedPoints()) {

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
                if (timelineElement.containsKey(TIMELINE_KEY_FRAMES)) {
                    frames = timelineElement.get(TIMELINE_KEY_FRAMES);
                } else {
                    frames = new ArrayList<Integer>();
                }
                frames.add(trajectoryPoint.getFrame());

                timelineElement.put(TIMELINE_KEY_FRAMES, frames);

                List<Poi> visible;
                if (timelineElement.containsKey(TIMELINE_KEY_POIS)) {
                    visible = timelineElement.get(TIMELINE_KEY_POIS);
                } else {
                    visible = new ArrayList<Poi>();
                }

                for (Poi poi : visiblePois) {
                    if (trajectoryPoint.isVisible(poi.getLatitude(), poi.getLongitude())) {
                        visible.add(poi);
                    }
                }

                timelineElement.put(TIMELINE_KEY_POIS, visible);

                timeline.put(position, timelineElement);
            }

            syncCache.put(timelineKey, timeline);
        }

        return timeline;
    }

    @Override public Poi getPoi(String placeId) throws Exception {
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
