package de.lmu.ifi.dbs.mediaqpoi.entity;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class TrajectoryPoint implements Serializable, Comparable<TrajectoryPoint> {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private int frame;

	@Persistent
	private double latitude;

	@Persistent
	private double longitude;

	@Persistent
	private double thetaX;

	@Persistent
	private double thetaY;

	@Persistent
	private double thetaZ;

	@Persistent
	private double r;

	@Persistent
	private int alpha;

	@Persistent
	private long timecode;

	public TrajectoryPoint(int frame, double latitude, double longitude, double thetaX, double thetaY, double thetaZ, double r, int alpha, long timecode) {
		this.frame = frame;
		this.latitude = latitude;
		this.longitude = longitude;
		this.thetaX = thetaX;
		this.thetaY = thetaY;
		this.thetaZ = thetaZ;
		this.r = r;
		this.alpha = alpha;
		this.timecode = timecode;
	}

	public int getFrame() {
		return frame;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getThetaX() {
		return thetaX;
	}

	public double getThetaY() {
		return thetaY;
	}

	public double getThetaZ() {
		return thetaZ;
	}

	public double getR() {
		return r;
	}

	public int getAlpha() {
		return alpha;
	}

	public long getTimecode() {
		return timecode;
	}

	@Override
	public int compareTo(TrajectoryPoint o) {
		if (o.getTimecode() < getTimecode()) {
			return -1;
		} else if (o.getTimecode() == getTimecode()) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public String toString() {
		return "frame: " + frame + ", latitude: " + latitude + ", longitude: " + longitude + ", thetaX: " + thetaX + ", thetaY: " + thetaY + ", thetaZ: " + thetaZ + ", timeCode: " + timecode;
	}
}