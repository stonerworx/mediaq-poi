package de.lmu.ifi.dbs.mediaqpoi.control.dataimport;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import de.lmu.ifi.dbs.mediaqpoi.control.PersistenceFacade;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

public class VideoImport {

	private static final Logger LOGGER = Logger.getLogger(VideoImport.class.getName());

	public List<Video> importData() throws Exception {
		LOGGER.info("Importing video_info and video_metadata from dump and persisting the video entities");
		
		DumpFileParser parser = new DumpFileParser();
		parser.parse("video_info.sql");
		parser.parse("video_metadata.sql");

		List<Video> videos = parser.getVideos();
		for (Video video : videos) {
			if (video.getTrajectory() != null && video.getTrajectory().getTimeStampedPoints() != null) {
				PersistenceFacade.persistVideo(video);
			}
		}
		
		return videos;
	}

	

}
