package de.lmu.ifi.dbs.mediaqpoi.entity;

import java.util.HashMap;
import java.util.Map;

import com.google.api.client.util.Key;

public class Trajectory {

	@Key
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
