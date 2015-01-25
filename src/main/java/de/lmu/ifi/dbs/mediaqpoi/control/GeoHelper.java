package de.lmu.ifi.dbs.mediaqpoi.control;

import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;

import java.util.logging.Logger;

public class GeoHelper {

    private static final Logger LOGGER = Logger.getLogger(GeoHelper.class.getName());
    // visibility range in meters
    public static final int VISIBILITY_RANGE = 100;

    /**
     * calculates the distance between two locations in meters
     *
     * @param loc1
     * @param loc2
     * @return distance in meters
     */
    public static double getDistanceInMeters(Location loc1, Location loc2) {

        // radius of the earth in km
        double r = 6371;

        double dLat = deg2rad(loc2.latitude - loc1.latitude);
        double dLon = deg2rad(loc2.longitude - loc1.longitude);

        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(loc1.latitude)) * Math.cos(deg2rad(loc2.latitude)) * Math.sin(dLon / 2) * Math
                .sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // distance in meters
        return r * c * 1000;

    }

    private static double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }

    /**
     * calculates the midpoint between two locations
     *
     * @param loc1
     * @param loc2
     * @return midpoint
     */
    public static Location getMidPoint(Location loc1, Location loc2) {

        double dLon = Math.toRadians(loc2.longitude - loc1.longitude);

        // convert to radians
        double loc1LatInRad = Math.toRadians(loc1.latitude);
        double loc2LatInRad = Math.toRadians(loc2.latitude);
        double loc1LonInRad = Math.toRadians(loc1.longitude);

        double Bx = Math.cos(loc2LatInRad) * Math.cos(dLon);
        double By = Math.cos(loc2LatInRad) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(loc1LatInRad) + Math.sin(loc2LatInRad),
            Math.sqrt((Math.cos(loc1LatInRad) + Bx) * (Math.cos(loc1LatInRad) + Bx) + By * By));
        double lon3 = loc1LonInRad + Math.atan2(By, Math.cos(loc1LatInRad) + Bx);

        return new Location(lat3, lon3);

    }

    /**
     * A trajectory here is assumed to be "in range" if at least one TrajectoryPoint lies in the
     * circumcircle around the range.
     */
    public static boolean isInRange(Trajectory trajectory, Location bound1, Location bound2) {
        if (trajectory == null || trajectory.getTimeStampedPoints() == null) {
            LOGGER.warning("No trajectory data to check.");
            return false;
        }

        Location middle = GeoHelper.getMidPoint(bound1, bound2);
        double distanceToMiddle = GeoHelper.getDistanceInMeters(bound1, middle);

        for (TrajectoryPoint point : trajectory.getTimeStampedPoints()) {
            Location pointLocation = new Location(point.getLatitude(), point.getLongitude());
            if (GeoHelper.getDistanceInMeters(pointLocation, middle) <= distanceToMiddle) {
                return true;
            }
        }

        return false;
    }

    /**
     * just for testing
     */
    public static void main(String[] args) {

        double lat1 = 48.152570;
        double lon1 = 11.592093;
        double lat2 = 48.152570;
        double lon2 = 11.591991;

        Location loc1 = new Location(lat1, lon1);
        Location loc2 = new Location(lat2, lon2);

        double d = GeoHelper.getDistanceInMeters(loc1, loc2);
        System.out.println("Distance in meters: " + d);

        Location mid = GeoHelper.getMidPoint(loc1, loc2);
        System.out.println("lat: " + Math.toDegrees(mid.latitude) + " lon: " + Math.toDegrees(mid.longitude));

        TrajectoryPoint trajectoryPoint = new TrajectoryPoint(0, lat1, lon1, 180, 0, 0, VISIBILITY_RANGE, 51, 0);
        boolean isVisible = trajectoryPoint.isVisible(lat2, lon2);
        System.out.println("Is visible: " + isVisible);

    }

}
