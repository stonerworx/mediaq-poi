package de.lmu.ifi.dbs.mediaqpoi.control.dataimport;

import de.lmu.ifi.dbs.mediaqpoi.control.PersistenceFacade;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

import java.util.List;
import java.util.logging.Logger;

public class VideoImport {

  private static final Logger LOGGER = Logger.getLogger(VideoImport.class.getName());

  public List<Video> importData() throws Exception {
    LOGGER.info("Importing video_info and video_metadata from dump and persisting the video entities");

    DumpFileParser parser = new DumpFileParser();
    parser.parse("video_info.sql");
    parser.parse("video_metadata.sql");

    LOGGER.info("Successfully parsed the dump files");

    List<Video> videos = parser.getVideos();
    LOGGER.info("Got " + videos.size() + " to import");

    for (Video video : videos) {
      if (video.getTrajectory() == null || video.getTrajectory().getTimeStampedPoints() == null) {
          LOGGER.info("Removing video without trajectory data from import");
          videos.remove(video);
      }
    }

    PersistenceFacade.persistVideos(videos);
    PersistenceFacade.indexVideos(videos);
    // TODO: fill RTree
    return videos;
  }
}
