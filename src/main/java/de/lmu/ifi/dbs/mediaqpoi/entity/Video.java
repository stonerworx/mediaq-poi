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
	private String fileName;

	@Persistent(dependent = "true")
	private Trajectory trajectory;

	@NotPersistent
	private Map<Integer, Map<String, List>> timeline = new HashMap<Integer, Map<String, List>>();

	@NotPersistent
	private String filePath;
	
	public Video(String fileName) {
		this.fileName = fileName;
		this.filePath = URL_VIDEO + fileName;
		Key key = KeyFactory.createKey(Video.class.getSimpleName(), fileName);
		setKey(key);
	}
	
	public Video() {
		super();
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String name) {
		this.fileName = name;
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
		return filePath;
	}

}
