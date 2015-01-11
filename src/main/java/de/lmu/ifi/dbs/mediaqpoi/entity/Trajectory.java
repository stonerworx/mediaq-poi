package de.lmu.ifi.dbs.mediaqpoi.entity;

import java.util.TreeMap;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Trajectory {

  	@PrimaryKey
  	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  	private com.google.appengine.api.datastore.Key key;

  	@Persistent
	private TreeMap<Long, TrajectoryPoint> timeStampedPoints;

	public TreeMap<Long, TrajectoryPoint> getTimeStampedPoints() {
		return timeStampedPoints;
	}

	public void setTimeStampedPoints(
			TreeMap<Long, TrajectoryPoint> timeStampedPoints) {
		this.timeStampedPoints = timeStampedPoints;
	}

	public void addPoint(long timeStamp, TrajectoryPoint point) {
		if (timeStampedPoints == null) {
			timeStampedPoints = new TreeMap<Long, TrajectoryPoint>();
		}
		timeStampedPoints.put(timeStamp, point);
	}

	public Location calculateCenter() {
		// TODO: implement
		return new Location(48.152187, 11.592492);
	}

	public long calculateSearchRange() {
		// TODO: implement
		return 500;
	}
}
