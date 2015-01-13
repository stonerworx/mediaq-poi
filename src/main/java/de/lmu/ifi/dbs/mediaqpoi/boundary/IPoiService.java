package de.lmu.ifi.dbs.mediaqpoi.boundary;

import java.util.List;
import java.util.Map;

import de.lmu.ifi.dbs.mediaqpoi.entity.Poi;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

public interface IPoiService {

	/**
	 * Returns the videos which record the given position (position must be in
	 * the visibility range of the video).
	 */
	public List<Video> getVideos(long longitude, long latitude) throws Exception;

	/**
	 * Returns the Pois that are visible in the video. The Pois are timeStamped
	 * with the seconds in the video runtime.
	 */
	public Map<Long, List<Poi>> getPois(Video video) throws Exception;
}
