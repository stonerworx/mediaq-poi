package de.lmu.ifi.dbs.mediaqpoi.entity;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import de.lmu.ifi.dbs.mediaqpoi.control.dataimport.DumpFileParser;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoRTree {

    private static final double MIN_LAT = Math.toRadians(-90d);
    private static final double MAX_LAT = Math.toRadians(90d);
    private static final double MIN_LON = Math.toRadians(-180d);
    private static final double MAX_LON = Math.toRadians(180d);
    private static int ID = 0;
    private SpatialIndex spatialIndex;
    private Map<Integer, Video> map;
    private SaveToListProcedure saveToListProcedure;

    public VideoRTree() {
        spatialIndex = new RTree();
        spatialIndex.init(null);
        map = new HashMap<Integer, Video>();
        saveToListProcedure = new SaveToListProcedure();
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

            VideoRTree videoRTree = new VideoRTree();
            videoRTree.insertAll(videos);
            List<Video> candidates = videoRTree.getCandidates(48.152588, 11.590518);
            for (Video video : candidates) {
                System.out.println(
                    video.getFileName() + " --> " + video.getTrajectory().getMaxLocation() + video
                        .getTrajectory().getMinLocation());
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
        Trajectory trajectory = video.getTrajectory();
        if (trajectory != null) {
            Location maxLocation = trajectory.getMaxLocation();
            Location minLocation = trajectory.getMinLocation();
            Rectangle mbr =
                new Rectangle((float) maxLocation.latitude, (float) maxLocation.longitude,
                    (float) minLocation.latitude, (float) minLocation.longitude);
            spatialIndex.add(mbr, ID);
            map.put(ID, video);
            ID++;
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
     * calculates the video candidates for a given poi location.
     *
     * @param latitude  of poi
     * @param longitude of poi
     * @return list of video candidates
     */
    public List<Video> getCandidates(double latitude, double longitude) {
        Location location = new Location(latitude, longitude);
        double distance = (double) Distance.VISIBILITY_RANGE / 1000;
        BoundingCoordinates boundingCoordinates = getBoundingCoordinates(location, distance);
        Location maxLocation = boundingCoordinates.getMaxLocation();
        Location minLocation = boundingCoordinates.getMinLocation();
        float maxLat = (float) maxLocation.latitude;
        float maxLon = (float) maxLocation.longitude;
        float minLat = (float) minLocation.latitude;
        float minLon = (float) minLocation.longitude;
        spatialIndex.intersects(new Rectangle(maxLat, maxLon, minLat, minLon), saveToListProcedure);
        return saveToListProcedure.getVideos();
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
        BoundingCoordinates boundingCoordinates = new BoundingCoordinates(maxLocation, minLocation);

        return boundingCoordinates;
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

        private List<Video> videos = new ArrayList<Video>();

        public boolean execute(int id) {
            videos.add(map.get(id));
            return true;
        }

        private List<Video> getVideos() {
            return videos;
        }

    }

}
