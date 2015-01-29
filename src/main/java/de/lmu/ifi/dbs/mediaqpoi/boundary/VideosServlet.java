package de.lmu.ifi.dbs.mediaqpoi.boundary;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.lmu.ifi.dbs.mediaqpoi.control.PersistenceFacade;
import de.lmu.ifi.dbs.mediaqpoi.control.PoiService;
import de.lmu.ifi.dbs.mediaqpoi.control.dataimport.VideoImport;
import de.lmu.ifi.dbs.mediaqpoi.entity.AlgorithmApproachType;
import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VideosServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VideosServlet.class.getName());
    private static final String ACTION_VIDEO_DETAILS = "details";
    private static final String ACTION_VIDEO_INITIAL_LOAD = "initial_load";
    private static final String ACTION_VIDEO_RANGE_QUERY = "range_query";
    private static final String ACTION_ALGO_CHANGE = "algo";

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json; charset=UTF-8");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "ok");

        String action = req.getParameter("action");

        switch ((action != null ? action : "")) {
            case ACTION_VIDEO_DETAILS:
                getVideoDetails(req, responseData);
                break;
            case ACTION_VIDEO_INITIAL_LOAD:
                performInitialLoad(responseData);
                break;
            case ACTION_VIDEO_RANGE_QUERY:
                getVideosInRange(req, responseData);
                break;
            case ACTION_ALGO_CHANGE:
                setAlgorithmApproach(req, responseData);
                break;
            default:
                getVideos(responseData);
                break;
        }

        Gson gson = new GsonBuilder()
          .excludeFieldsWithoutExposeAnnotation()
          .create();

        resp.getWriter().write(gson.toJson(responseData));
    }

    private void setAlgorithmApproach(HttpServletRequest req, Map<String, Object> responseData) {
        try {
            if (req.getParameter("approach") == null) {
                throw new IllegalArgumentException("Approach must not be null");
            }
            PoiService.setApproach(AlgorithmApproachType.valueOf(req.getParameter("approach")));
            responseData.put("status", "ok");
            responseData.put("message", "Algorithm approach successfully changed to " + PoiService.getApproach().name());
        } catch (Exception e) {
            LOGGER.severe(String
                .format("Exception occurred while changing algorithm approach: %s", e));
            responseData.put("status", "error");
            responseData.put("message", "Algorithm approach not changed.");
        }
    }

    private void getVideosInRange(HttpServletRequest req, Map<String, Object> responseData) {
        try {
            if (req.getParameter("bound1_lat") == null) {
                throw new IllegalArgumentException("Latitude of first bound must not be null");
            }
            if (req.getParameter("bound1_lng") == null) {
                throw new IllegalArgumentException("Longitude of first bound must not be null");
            }
            if (req.getParameter("bound2_lat") == null) {
                throw new IllegalArgumentException("Latitude of second bound must not be null");
            }
            if (req.getParameter("bound2_lng") == null) {
                throw new IllegalArgumentException("Longitude of second bound must not be null");
            }
            double bound1Lat = Double.parseDouble(req.getParameter("bound1_lat"));
            double bound1Lon = Double.parseDouble(req.getParameter("bound1_lng"));
            double bound2Lat = Double.parseDouble(req.getParameter("bound2_lat"));
            double bound2Lon = Double.parseDouble(req.getParameter("bound2_lng"));

            Location northEast = new Location(bound1Lat, bound1Lon);
            Location southWest = new Location(bound2Lat, bound2Lon);
            List<Video> videos = PoiService.getInstance().getVideosInRange(northEast, southWest);
            if (!videos.isEmpty()) {
                for (Video video : videos) {
                    // "touch" child entities to load them
                    video.touch();
                    videos.add(video);
                }
                responseData.put("videos", videos);
            }
        } catch (Exception e) {
            LOGGER.severe(String.format("Exception occurred while performing range query for videos: %s", e));
            responseData.put("status", "error");
            responseData.put("message", "Videos in range could not be loaded.");
        }
    }

    private void performInitialLoad(Map<String, Object> response) {
        try {
            new VideoImport().importData();
            response.put("status", "ok");
            response.put("message", "Videos successfully loaded.");
        } catch (Exception e) {
            LOGGER.severe(String
                .format("Exception occurred while performing inital " + "load of videos: %s", e));
            response.put("status", "error");
            response.put("message", "Videos not initially loaded.");
        }
    }

    private void getVideos(Map<String, Object> response) {
        LOGGER.info("Getting all videos");

        List<Video> videos = new ArrayList<>();

        try {
            List<Video> results = PersistenceFacade.getVideos();
            if (!results.isEmpty()) {
                for (Video video : results) {
                    // "touch" child entities to load them
                    video.getTrajectory().getTimeStampedPoints();
                    videos.add(video);
                }
            } else {
                videos.addAll(new VideoImport().importData());
            }
        } catch (Exception e) {
            LOGGER.severe(String.format("Exception occurred while getting videos: %s", e));
        }

        response.put("videos", videos);
    }

    private void getVideoDetails(HttpServletRequest request, Map<String, Object> response) {
        Key key = null;
        try {
            String id = request.getParameter("id");
            if (id == null) {
                throw new IllegalArgumentException("Id must not be null");
            }
            LOGGER.info("Getting details for video id " + id);
            key = KeyFactory.createKey(Video.class.getSimpleName(), id);

            Video video = PersistenceFacade.getVideo(key);
            video.touch();

            response.put("video", video);
            response.put("center", video.getTrajectory().getCenter());
            response.put("searchRange", video.getTrajectory().getSearchRange());
            response.put("nearbyPois", PoiService.getInstance().getPoiCandidates(video));
            response.put("visiblePois", PoiService.getInstance().getVisiblePois(video));
            response.put("timeline", PoiService.getInstance().getPois(video));
            response.put("posTimeline", video.getTrajectory().getTimeLine());
        } catch (Exception e) {
            LOGGER.severe(
                String.format("Exception occurred while getting video with id %s: %s", key, e));
            response.put("status", "error");
            response.put("message", "Video not found.");
        }
    }

}
