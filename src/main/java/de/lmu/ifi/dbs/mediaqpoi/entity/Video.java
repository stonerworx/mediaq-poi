package de.lmu.ifi.dbs.mediaqpoi.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable
public class Video {
	private static final String URL_VIDEO = "http://mediaq.dbs.ifi.lmu.de/MediaQ_MVC_V2/video_content/";
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private String title;

	@Persistent
	private String id;

	@Persistent(dependent = "true")
	private Trajectory trajectory;

	@NotPersistent
	private Map<Integer, Map<String, List>> timeline = new HashMap<Integer, Map<String, List>>();

	public Video(String id, String title) {
		this.id = id;
		this.title = title;
		Key key = KeyFactory.createKey(Video.class.getSimpleName(), id);
		setKey(key);
	}

	/** Simple constructor just for testing the DumpFileParser */
	public Video(String id) {
		this.id = id;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void addTrajectoryPoint(long timeStamp, TrajectoryPoint point) {
		// trajectory.addPoint(timeStamp, point);
	}

	public void setTrajectory(Trajectory trajectory) {
		this.trajectory = trajectory;
	}

	public Trajectory getTrajectory() {
		return trajectory;
	}

	public String getFilePath() {
		return URL_VIDEO + id;
	}

}
