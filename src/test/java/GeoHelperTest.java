import de.lmu.ifi.dbs.mediaqpoi.control.GeoHelper;
import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;

import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

public class GeoHelperTest {

  @Test
  public void testIsInRangeTrue() {
    Location northEast = new Location(48.153212, 11.590362);
    Location southWest = new Location(48.152188, 11.593956);
    TrajectoryPoint point = new TrajectoryPoint(0, 48.152575, 11.592122, 0, 0, 0, 0, 0, 0);
    Trajectory trajectory = new Trajectory();
    TreeSet<TrajectoryPoint> points = new TreeSet();
    points.add(point);
    trajectory.setTimeStampedPoints(points);
    Assert.assertTrue("Trajectory should be in range", GeoHelper.isInRange(trajectory, northEast, southWest));
  }

  @Test
  public void testIsInRangeFalse() {
    Location northEast = new Location(48.153212, 11.590362);
    Location southWest = new Location(48.152188, 11.593956);
    TrajectoryPoint point = new TrajectoryPoint(0, 49.090383, 10.836704, 0, 0, 0, 0, 0, 0);
    Trajectory trajectory = new Trajectory();
    TreeSet<TrajectoryPoint> points = new TreeSet();
    points.add(point);
    trajectory.setTimeStampedPoints(points);
    Assert.assertFalse("Trajectory should not be in range", GeoHelper.isInRange(trajectory, northEast, southWest));
  }
}
