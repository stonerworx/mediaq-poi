package de.lmu.ifi.dbs.mediaqpoi.boundary;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

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

        Gson gson = new Gson();

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
                    video.getTrajectory().getTimeStampedPoints();
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

    @SuppressWarnings("unused")
    private void generateTestData(PersistenceManager pm, List<Video> videos) {
        LOGGER.info("Generating test data");

        Video video1 = new Video("1n940cuzuw3fi_2014_12_8_Videotake_1418038438174.mp4");
        Trajectory trajectory = new Trajectory();
        video1.setTrajectory(trajectory);

        TreeSet<TrajectoryPoint> points = new TreeSet<>();

        TrajectoryPoint tp1 =
            new TrajectoryPoint(1, 48.152187, 11.592492, 352.28128, -9.597204, 87.91111, 0.1, 51,
                1418038438811L);
        points.add(tp1);

        TrajectoryPoint tp2 =
            new TrajectoryPoint(2, 48.152187, 11.592492, 348.31525, -11.656581, 89.825325, 0.1, 51,
                1418038438814L);
        points.add(tp2);

        TrajectoryPoint tp3 =
            new TrajectoryPoint(3, 48.152187, 11.592492, 349.32913, -12.251886, 89.30554, 0.1, 51,
                1418038438814L);
        points.add(tp3);

        TrajectoryPoint tp4 =
            new TrajectoryPoint(4, 48.152187, 11.592492, 352.6046, -13.812843, 88.258095, 0.1, 51,
                1418038438814L);
        points.add(tp4);

        TrajectoryPoint tp5 =
            new TrajectoryPoint(5, 48.152187, 11.592492, 351.61664, -11.474263, 88.01563, 0.1, 51,
                1418038438814L);
        points.add(tp5);

        TrajectoryPoint tp6 =
            new TrajectoryPoint(6, 48.152187, 11.592492, 348.57904, -13.385277, 89.00518, 0.1, 51,
                1418038438814L);
        points.add(tp6);

        TrajectoryPoint tp7 =
            new TrajectoryPoint(7, 48.152187, 11.592492, 346.19858, -15.075933, 89.19742, 0.1, 51,
                1418038438814L);
        points.add(tp7);

        TrajectoryPoint tp8 =
            new TrajectoryPoint(8, 48.152403, 11.59219, 347.97443, -15.249314, 85.45663, 0.1, 51,
                1418038451612L);
        points.add(tp8);

        TrajectoryPoint tp9 =
            new TrajectoryPoint(9, 48.152403, 11.59219, 345.38354, -12.490302, 86.31679, 0.1, 51,
                1418038451612L);
        points.add(tp9);

        TrajectoryPoint tp10 =
            new TrajectoryPoint(10, 48.152403, 11.59219, 345.81836, -11.510367, 89.53561, 0.1, 51,
                1418038451612L);
        points.add(tp10);

        TrajectoryPoint tp11 =
            new TrajectoryPoint(11, 48.152403, 11.59219, 1.5293881, -16.820906, 82.52942, 0.1, 51,
                1418038451612L);
        points.add(tp11);

        TrajectoryPoint tp12 =
            new TrajectoryPoint(12, 48.152403, 11.59219, 2.5045202, -6.9362087, 92.23532, 0.1, 51,
                1418038451612L);
        points.add(tp12);

        TrajectoryPoint tp13 =
            new TrajectoryPoint(13, 48.152403, 11.59219, 15.032611, -5.8767695, 93.21849, 0.1, 51,
                1418038451612L);
        points.add(tp13);

        TrajectoryPoint tp14 =
            new TrajectoryPoint(14, 48.152403, 11.59219, 35.903744, -6.1979914, 85.88856, 0.1, 51,
                1418038451612L);
        points.add(tp14);

        TrajectoryPoint tp15 =
            new TrajectoryPoint(15, 48.152403, 11.59219, 57.900295, -8.766266, 89.89291, 0.1, 51,
                1418038452612L);
        points.add(tp15);

        TrajectoryPoint tp16 =
            new TrajectoryPoint(16, 48.152477, 11.591747, 70.55008, 2.390737, 89.59158, 0.1, 51,
                1418038453613L);
        points.add(tp16);

        TrajectoryPoint tp17 =
            new TrajectoryPoint(17, 48.152477, 11.591747, 92.186195, -2.2912872, 87.64133, 0.1, 51,
                1418038453613L);
        points.add(tp17);

        TrajectoryPoint tp18 =
            new TrajectoryPoint(18, 48.152477, 11.591747, 119.959274, -6.0796943, 89.54608, 0.1, 51,
                1418038453613L);
        points.add(tp18);

        TrajectoryPoint tp19 =
            new TrajectoryPoint(19, 48.152477, 11.591747, 354.76465, -11.521731, 80.601326, 0.1, 51,
                1418038453613L);
        points.add(tp19);

        TrajectoryPoint tp20 =
            new TrajectoryPoint(20, 48.152477, 11.591747, 323.30145, -12.656222, 89.52736, 0.1, 51,
                1418038461612L);
        points.add(tp20);

        TrajectoryPoint tp21 =
            new TrajectoryPoint(21, 48.152477, 11.591747, 347.73843, -1.4339141, 91.73121, 0.1, 51,
                1418038461612L);
        points.add(tp21);

        TrajectoryPoint tp22 =
            new TrajectoryPoint(22, 48.152477, 11.591747, 6.2104645, -3.822386, 87.04369, 0.1, 51,
                1418038461612L);
        points.add(tp22);

        TrajectoryPoint tp23 =
            new TrajectoryPoint(23, 48.152477, 11.591747, 18.317919, -2.014577, 88.32025, 0.1, 51,
                1418038461612L);
        points.add(tp23);

        TrajectoryPoint tp24 =
            new TrajectoryPoint(24, 48.152477, 11.591747, 37.418358, -9.7401705, 87.426, 0.1, 51,
                1418038461612L);
        points.add(tp24);

        TrajectoryPoint tp25 =
            new TrajectoryPoint(25, 48.152477, 11.591747, 51.85172, -11.179051, 91.539246, 0.1, 51,
                1418038461612L);
        points.add(tp25);

        TrajectoryPoint tp26 =
            new TrajectoryPoint(26, 48.152477, 11.591747, 61.355568, 0.8634349, 90, 0.1, 51,
                1418038461612L);
        points.add(tp26);

        TrajectoryPoint tp27 =
            new TrajectoryPoint(27, 48.152301, 11.591794, 71.7291, 1.4771047, 87.721695, 0.1, 51,
                1418038467413L);
        points.add(tp27);

        TrajectoryPoint tp28 =
            new TrajectoryPoint(28, 48.152301, 11.591794, 74.00962, 1.1116854, 92.75136, 0.1, 51,
                1418038467413L);
        points.add(tp28);

        TrajectoryPoint tp29 =
            new TrajectoryPoint(29, 48.152301, 11.591794, 132.85698, -15.295082, 86.193474, 0.1, 51,
                1418038467413L);
        points.add(tp29);

        TrajectoryPoint tp30 =
            new TrajectoryPoint(30, 48.152301, 11.591794, 288.69293, -13.687717, 91.23135, 0.1, 51,
                1418038467413L);
        points.add(tp30);

        TrajectoryPoint tp31 =
            new TrajectoryPoint(31, 48.152301, 11.591794, 339.52197, -1.5164657, 86.17897, 0.1, 51,
                1418038467413L);
        points.add(tp31);

        TrajectoryPoint tp32 =
            new TrajectoryPoint(32, 48.152301, 11.591794, 344.17014, -1.3188993, 89.057724, 0.1, 51,
                1418038471413L);
        points.add(tp32);

        TrajectoryPoint tp33 =
            new TrajectoryPoint(33, 48.152301, 11.591794, 349.5732, -4.9663854, 91.14905, 0.1, 51,
                1418038471413L);
        points.add(tp33);

        TrajectoryPoint tp34 =
            new TrajectoryPoint(34, 48.152301, 11.591794, 353.74518, -6.8766623, 89.59692, 0.1, 51,
                1418038471413L);
        points.add(tp34);

        TrajectoryPoint tp35 =
            new TrajectoryPoint(35, 48.152301, 11.591794, 352.2595, -7.3414783, 89.71608, 0.1, 51,
                1418038472413L);
        points.add(tp35);

        TrajectoryPoint tp36 =
            new TrajectoryPoint(36, 48.15212, 11.592853, 353.71088, -7.28784, 87.59499, 0.1, 51,
                1418038473613L);
        points.add(tp36);

        TrajectoryPoint tp37 =
            new TrajectoryPoint(37, 48.152301, 11.591794, 351.1022, -6.0824943, 89.087, 0.1, 51,
                1418038474413L);
        points.add(tp37);

        TrajectoryPoint tp38 =
            new TrajectoryPoint(38, 48.152301, 11.591794, 350.38318, -6.506596, 88.57926, 0.1, 51,
                1418038474413L);
        points.add(tp38);

        TrajectoryPoint tp39 =
            new TrajectoryPoint(39, 48.152301, 11.591794, 353.1969, -7.4808955, 87.98944, 0.1, 51,
                1418038477413L);
        points.add(tp39);

        TrajectoryPoint tp40 =
            new TrajectoryPoint(40, 48.152301, 11.591794, 351.5411, -7.097056, 90.28308, 0.1, 51,
                1418038477413L);
        points.add(tp40);

        TrajectoryPoint tp41 =
            new TrajectoryPoint(41, 48.152301, 11.591794, 350.73282, -6.459255, 89.196304, 0.1, 51,
                1418038477413L);
        points.add(tp41);

        TrajectoryPoint tp42 =
            new TrajectoryPoint(42, 48.152301, 11.591794, 350.31232, -7.5636954, 88.97798, 0.1, 51,
                1418038481413L);
        points.add(tp42);

        TrajectoryPoint tp43 =
            new TrajectoryPoint(43, 48.152301, 11.591794, 350.75174, -6.436091, 87.82598, 0.1, 51,
                1418038481413L);
        points.add(tp43);

        TrajectoryPoint tp44 =
            new TrajectoryPoint(44, 48.152301, 11.591794, 349.6996, -7.519072, 87.95257, 0.1, 51,
                1418038481413L);
        points.add(tp44);

        TrajectoryPoint tp45 =
            new TrajectoryPoint(45, 48.152301, 11.591794, 350.4762, -7.0324507, 87.45006, 0.1, 51,
                1418038481413L);
        points.add(tp45);

        TrajectoryPoint tp46 =
            new TrajectoryPoint(46, 48.152301, 11.591794, 349.59186, -7.1229205, 88.60282, 0.1, 51,
                1418038481413L);
        points.add(tp46);

        TrajectoryPoint tp47 =
            new TrajectoryPoint(47, 48.152301, 11.591794, 348.4882, -7.3259916, 88.40093, 0.1, 51,
                1418038481413L);
        points.add(tp47);

        TrajectoryPoint tp48 =
            new TrajectoryPoint(48, 48.152304, 11.591875, 349.9612, -6.6093545, 86.11949, 0.1, 51,
                1418038489413L);
        points.add(tp48);

        TrajectoryPoint tp49 =
            new TrajectoryPoint(49, 48.152304, 11.591875, 349.9788, -6.8469205, 87.83893, 0.1, 51,
                1418038489413L);
        points.add(tp49);

        TrajectoryPoint tp50 =
            new TrajectoryPoint(50, 48.152304, 11.591875, 351.91995, -6.3524337, 86.2521, 0.1, 51,
                1418038489413L);
        points.add(tp50);

        TrajectoryPoint tp51 =
            new TrajectoryPoint(51, 48.152304, 11.591875, 349.5256, -8.352057, 85.93989, 0.1, 51,
                1418038489413L);
        points.add(tp51);

        TrajectoryPoint tp52 =
            new TrajectoryPoint(52, 48.152304, 11.591875, 348.69537, -6.792612, 87.360985, 0.1, 51,
                1418038489413L);
        points.add(tp52);

        TrajectoryPoint tp53 =
            new TrajectoryPoint(53, 48.152307, 11.591866, 349.0546, -6.2741165, 88.013336, 0.1, 51,
                1418038490413L);
        points.add(tp53);

        TrajectoryPoint tp54 =
            new TrajectoryPoint(54, 48.152311, 11.591859, 347.8421, -6.047685, 88.16716, 0.1, 51,
                1418038491413L);
        points.add(tp54);

        TrajectoryPoint tp55 =
            new TrajectoryPoint(55, 48.152315, 11.591852, 350.1987, -6.2320614, 85.534645, 0.1, 51,
                1418038492412L);
        points.add(tp55);

        TrajectoryPoint tp56 =
            new TrajectoryPoint(56, 48.152201, 11.592368, 337.78912, -5.85147, 89.82207, 0.1, 51,
                1418038493613L);
        points.add(tp56);

        TrajectoryPoint tp57 =
            new TrajectoryPoint(57, 48.152187, 11.592482, 335.95313, -8.423289, 87.86395, 0.1, 51,
                1418038494413L);
        points.add(tp57);

        TrajectoryPoint tp58 =
            new TrajectoryPoint(58, 48.152342, 11.591816, 334.41403, -7.1757455, 92.29061, 0.1, 51,
                1418038495412L);
        points.add(tp58);

        TrajectoryPoint tp59 =
            new TrajectoryPoint(59, 48.15235, 11.591812, 343.4911, -4.4248486, 82.00547, 0.1, 51,
                1418038496415L);
        points.add(tp59);

        TrajectoryPoint tp60 =
            new TrajectoryPoint(60, 48.152354, 11.591813, 325.77124, -7.1749935, 90.34901, 0.1, 51,
                1418038497412L);
        points.add(tp60);

        TrajectoryPoint tp61 =
            new TrajectoryPoint(61, 48.152358, 11.591813, 336.7439, -14.83654, 86.35028, 0.1, 51,
                1418038498412L);
        points.add(tp61);

        TrajectoryPoint tp62 =
            new TrajectoryPoint(62, 48.152361, 11.591813, 15.102279, -16.746452, 95.28025, 0.1, 51,
                1418038499412L);
        points.add(tp62);

        video1.getTrajectory().setTimeStampedPoints(points);
        videos.add(video1);

        try {
            PersistenceFacade.persistVideo(video1);
        } catch (Exception e) {
            LOGGER.info("Testvideo not persisted");
        }
    }
}
