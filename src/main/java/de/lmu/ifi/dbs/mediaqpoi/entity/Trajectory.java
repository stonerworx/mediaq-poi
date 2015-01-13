package de.lmu.ifi.dbs.mediaqpoi.entity;

import java.util.TreeSet;

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
	private TreeSet<TrajectoryPoint> timeStampedPoints;
	
//    /*
//     * Important: Do not create getter and setters for this object else
//     * bidirectional mapping gives error
//     */
//	@Persistent(mappedBy = "trajectory")
//	private Video video;
	
	@Persistent
	private String test = "B L A";

	public TreeSet<TrajectoryPoint> getTimeStampedPoints() {
		return timeStampedPoints;
	}

	public void setTimeStampedPoints(TreeSet<TrajectoryPoint> timeStampedPoints) {
		this.timeStampedPoints = timeStampedPoints;
	}

	public Location calculateCenter() {
		// TODO: implement
		return new Location(48.152187, 11.592492);
	}

	public long calculateSearchRange() {
		// TODO: implement
		return 500;
	}

	public com.google.appengine.api.datastore.Key getKey() {
		return key;
	}

	public void setKey(com.google.appengine.api.datastore.Key key) {
		this.key = key;
	}

	public long getStartTime() {
		if(timeStampedPoints != null) {
			return ((TreeSet<TrajectoryPoint>) timeStampedPoints).first().getTimecode();
		}
		else {
			return -1;
		}
	}
	
	public long getEndTime() {
		if(timeStampedPoints != null) {
			return ((TreeSet<TrajectoryPoint>) timeStampedPoints).last().getTimecode();
		}
		else {
			return -1;
		}
	}
	
	public TrajectoryPoint getStartPoint() {
		if(timeStampedPoints != null) {
			return ((TreeSet<TrajectoryPoint>) timeStampedPoints).first();
		}
		else {
			return null;
		}
	}
	
	public TrajectoryPoint getEndPoint() {
		if(timeStampedPoints != null) {
			return ((TreeSet<TrajectoryPoint>) timeStampedPoints).last();
		}
		else {
			return null;
		}
	}
}
