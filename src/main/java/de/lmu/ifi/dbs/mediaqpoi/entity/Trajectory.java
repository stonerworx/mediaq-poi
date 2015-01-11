package de.lmu.ifi.dbs.mediaqpoi.entity;

import java.util.HashMap;
import java.util.Map;

import com.google.api.client.util.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Trajectory {

  	@PrimaryKey
  	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  	private com.google.appengine.api.datastore.Key key;

  	@Persistent(serialized = "true", defaultFetchGroup="true")
	private Map<Long, TrajectoryPoint> timeStampedPoints;

	public Map<Long, TrajectoryPoint> getTimeStampedPoints() {
		return timeStampedPoints;
	}

	public void setTimeStampedPoints(
			Map<Long, TrajectoryPoint> timeStampedPoints) {
		this.timeStampedPoints = timeStampedPoints;
	}

	public void addPoint(long timeStamp, TrajectoryPoint point) {
		if (timeStampedPoints == null) {
			timeStampedPoints = new HashMap<Long, TrajectoryPoint>();
		} else {
		  	Map<Long, TrajectoryPoint> oldTimeStampedPoints = timeStampedPoints;
		  	timeStampedPoints = new HashMap<Long, TrajectoryPoint>();
		  	timeStampedPoints = oldTimeStampedPoints;
		}
		timeStampedPoints.put(timeStamp, point);
	}

	public Location calculateCenter() {
		// TODO: implement
		return null;
	}

	public long calculateSearchRange() {
		// TODO: implement
		return -1;
	}
}
