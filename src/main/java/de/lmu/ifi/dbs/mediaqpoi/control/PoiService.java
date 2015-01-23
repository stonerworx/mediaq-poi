package de.lmu.ifi.dbs.mediaqpoi.control;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import de.lmu.ifi.dbs.mediaqpoi.boundary.IPoiService;
import de.lmu.ifi.dbs.mediaqpoi.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PoiService implements IPoiService {

    private static final Logger LOGGER = Logger.getLogger(PoiService.class.getName());
    private final static IPoiService instance = new PoiService();

    public static IPoiService getInstance() {
        return instance;
    }

    @Override public List<Video> getVideos(long longitude, long latitude) throws Exception {
        // TODO: non naive approach using index
        return getVideosNaive(longitude, latitude);
    }

    @Override public Map<Long, List<Poi>> getPois(Video video) throws Exception {
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

    @SuppressWarnings("unchecked") @Override public List<Poi> getPoiCandidates(Video video) throws Exception {
        String key = "nearbyPois_" + video.getKey();

        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        List<Poi> nearbyPois = (ArrayList<Poi>) syncCache.get(key);
        if (nearbyPois == null) {
            nearbyPois = new ArrayList<>();

            PlacesList places = GooglePlacesApi.searchPlaces(video.getTrajectory().calculateCenter(), video.getTrajectory().calculateSearchRange());

            for (Place place : places.results) {
                Poi poi = new Poi(place);
                nearbyPois.add(poi);
            }

            syncCache.put(key, nearbyPois);
        }

        return nearbyPois;
    }

    @SuppressWarnings("unchecked") @Override public List<Poi> getVisiblePois(Video video) throws Exception {
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

        return visiblePois;
    }

    @Override public Poi getPoi(String placeId) throws Exception {
        Place place = GooglePlacesApi.getDetails(placeId);
        if (place != null) {
            return new Poi(place);
        }
        return null;
    }

    @Override public List<Video> getVideosInRange(Location min, Location max) throws Exception {
        //TODO: naive approach
        return PersistenceFacade.getVideosInRange(min, max);
    }

    /**
     * Naive approach for getting the videos that record a given geo location. All videos are retrieved and iterated.
     */
    private List<Video> getVideosNaive(long longitude, long latitude) throws Exception {
        try {
            List<Video> allVideos = PersistenceFacade.getVideos();
            List<Video> result = new ArrayList<>();

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
            return result;

        } catch (Exception e) {
            LOGGER.severe("Exception occurred in naive approach of getting all videos recording a specific geo location: " + e);
            throw e;
        }
    }
}
