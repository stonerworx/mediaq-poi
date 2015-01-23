package de.lmu.ifi.dbs.mediaqpoi.boundary;

import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Poi;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

import java.util.List;
import java.util.Map;

public interface IPoiService {

    /**
     * Returns the videos in the given map bounds.
     */
    public List<Video> getVideosInRange(Location min, Location max) throws Exception;

    /**
     * Returns the videos which record the given position (position must be in the visibility range of
     * the video).
     */
    public List<Video> getVideos(double longitude, double latitude) throws Exception;

    /**
     * Returns all Pois that are located in the search range of the video and could therefore be potentially visible in the video.
     */
    public List<Poi> getPoiCandidates(Video video) throws Exception;

    /**
     * Returns all visible Pois in the video.
     */
    public List<Poi> getVisiblePois(Video video) throws Exception;

    /**
     * Returns the Pois that are visible in the video. The Pois are timeStamped with the seconds in
     * the video runtime.
     */
    public Map<Long, List<Poi>> getPois(Video video) throws Exception;

    /**
     * Returns the Poi to the given placeId
     */
    public Poi getPoi(String placeId) throws Exception;

}
