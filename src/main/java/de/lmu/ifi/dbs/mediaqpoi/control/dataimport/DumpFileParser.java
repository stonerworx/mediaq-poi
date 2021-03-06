package de.lmu.ifi.dbs.mediaqpoi.control.dataimport;

import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class DumpFileParser {

  private static final Logger LOGGER = Logger.getLogger(DumpFileParser.class.getName());
  private Map<String, Video> videos = new HashMap<>();

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

        Video video ;
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
      int patternCount = StringUtils.countMatches(fileContent, "INSERT INTO");
      String insertStatementPart = Pattern.compile("\\A.+((INSERT INTO `VIDEO_METADATA` VALUES.+?\\);\\n){" + patternCount + "}).+\\Z",
              Pattern.DOTALL).matcher(fileContent).replaceFirst("$1");

      String insertStatements[] = insertStatementPart.split("\\n");
      for (String insertStatement : insertStatements) {
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
              new TrajectoryPoint(Integer.parseInt(frame), Double.parseDouble(latitude),
                                  Double.parseDouble(longitude), Double.parseDouble(thetaX),
                                  Double.parseDouble(thetaY),
                                  Double.parseDouble(thetaZ), Double.parseDouble(r),
                                  Integer.parseInt(alpha), Long.parseLong(timeCode));

          if (videos.isEmpty()) {
            throw new Exception("Please import video_info first");
          } else {
            Video video = videos.get(name);

            if (video == null) {
              LOGGER.warning("Got metadata for nonexistent video " + name);
            } else {
              Trajectory trajectory = video.getTrajectory();
              if (trajectory == null) {
                trajectory = new Trajectory();
                trajectory.setTimeStampedPoints(new TreeSet<TrajectoryPoint>());
              }
              trajectory.add(point);
              video.setTrajectory(trajectory);
            }

            videos.put(video.getFileName(), video);
          }
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
    List<Video> videos = new CopyOnWriteArrayList<>();
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
      e.printStackTrace();
    }
  }

}
