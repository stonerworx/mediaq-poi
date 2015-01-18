package de.lmu.ifi.dbs.mediaqpoi.control.dataimport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

public class DumpFileParser {

  private static final Logger LOGGER = Logger.getLogger(DumpFileParser.class.getName());
  private Map<String, Video> videos = new HashMap<String, Video>();

  public void parse(String fileName) throws Exception {
    String fileContent = readFile(fileName);

    if (fileContent.contains("VIDEO_INFO")) {
      parseVideoInfo(fileContent);
    } else if (fileContent.contains("VIDEO_METADATA")) {
      parseVideoMetaData(fileContent);
    }
  }

  private void parseVideoInfo(String fileContent) throws Exception {
    try {
      String insertStatement = Pattern.compile("\\A.*(INSERT INTO `VIDEO_INFO` VALUES.+?;).*\\Z", Pattern.DOTALL).matcher(fileContent).replaceAll("$1");
      insertStatement = insertStatement.replaceFirst("INSERT INTO `VIDEO_INFO` VALUES \\(", "\\(");
      insertStatement = insertStatement.substring(0, insertStatement.length() - 1);

      String[] insertRows = insertStatement.split("\\),\\(");

      for (String row : insertRows) {
        row = row.replace("(", "").replace(")", "");
        String[] insertValues = row.split("','");

        String name = insertValues[0].replace("'", "");

        Video video = null;
        try {
          video = new Video(name);
        } catch (Exception e) {
          video = new Video(); // happens when tested via main method
          video.setFileName(name);
        }
        videos.put(name, video);
      }
    } catch (Exception e) {
      LOGGER.severe("Exception while parsing video info: " + e);
    }
  }

  private void parseVideoMetaData(String fileContent) throws Exception {
    try {
      String insertStatement = Pattern.compile("\\A.*(INSERT INTO `VIDEO_METADATA` VALUES.+?;).*\\Z", Pattern.DOTALL).matcher(fileContent).replaceAll("$1");
      insertStatement = insertStatement.replaceFirst("INSERT INTO `VIDEO_METADATA` VALUES \\(", "\\(");
      insertStatement = insertStatement.substring(0, insertStatement.length() - 1);

      String[] insertRows = insertStatement.split("\\),\\(");

      for (String row : insertRows) {
        row = row.replace("(", "").replace(")", "");
        String[] insertValues = row.split(",");

        String name = insertValues[0].replace("'", "");

        String frame = insertValues[1].replace("'", "");
        String latitude = insertValues[2].replace("'", "");
        String longitude = insertValues[3].replace("'", "");
        String thetaX = insertValues[4].replace("'", "");
        String thetaY = insertValues[5].replace("'", "");
        String thetaZ = insertValues[6].replace("'", "");
        String r = insertValues[7].replace("'", "");
        String alpha = insertValues[8].replace("'", "");
        String timeCode = insertValues[9].replace("'", "");

        TrajectoryPoint point =
            new TrajectoryPoint(Integer.parseInt(frame), Double.parseDouble(latitude), Double.parseDouble(longitude), Double.parseDouble(thetaX), Double.parseDouble(thetaY),
                Double.parseDouble(thetaZ), Double.parseDouble(r), Integer.parseInt(alpha), Long.parseLong(timeCode));

        if (videos.isEmpty()) {
          throw new Exception("Please import video_info first");
        } else {
          Video video = videos.get(name);

          if (video == null) {
            LOGGER.warning("Got metadata for nonexistent video " + name);
          } else {
            if (video.getTrajectory() == null) {
              video.setTrajectory(new Trajectory());
              video.getTrajectory().setTimeStampedPoints(new TreeSet<TrajectoryPoint>());
            }
            Trajectory trajectory = video.getTrajectory();
            trajectory.add(point);
            video.setTrajectory(trajectory);
          }

          videos.put(video.getFileName(), video);
        }
      }
    } catch (Exception e) {
      LOGGER.severe("Exception while parsing video metadata: " + e);
    }
  }

  private String readFile(String fileName) throws Exception {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName); Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
      return scanner.hasNext() ? scanner.next() : "";
    } catch (Exception e) {
      LOGGER.severe("Exception while reading file: " + e);
      throw e;
    }
  }

  public List<Video> getVideos() {
    List<Video> videos = new ArrayList<Video>();
    videos.addAll(this.videos.values());
    return videos;
  }

  /**
   * Just for testing (set working directory to src/main/resources!!
   */
  public static void main(String args[]) {
    try {
      DumpFileParser parser = new DumpFileParser();
      parser.parse("video_info.sql");
      for (Video video : parser.getVideos()) {
        System.out.println(video.getFileName());
      }

      parser.parse("video_metadata.sql");
      for (Video video : parser.getVideos()) {
        if (video.getTrajectory() == null || video.getTrajectory().getTimeStampedPoints() == null) {
          System.out.println("Video has no trajectory " + video.getFileName());
          continue;
        }
        for (TrajectoryPoint point : video.getTrajectory().getTimeStampedPoints()) {
          System.out.println(point);
        }
      }
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

}
