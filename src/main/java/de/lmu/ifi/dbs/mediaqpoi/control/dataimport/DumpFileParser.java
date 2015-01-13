package de.lmu.ifi.dbs.mediaqpoi.control.dataimport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

public class DumpFileParser {

	private static final Logger LOGGER = Logger.getLogger(DumpFileParser.class.getName());
	private List<Video> videos = new ArrayList<Video>();

	public void parse(String fileName) throws Exception {
		String fileContent = readFile(fileName);

		if (fileContent.contains("VIDEO_INFO")) {
			parseVideoInfo(fileContent);
		}
	}

	private void parseVideoInfo(String fileContent) throws Exception {
		try {
			String insertStatement = Pattern.compile("\\A.*(INSERT INTO `VIDEO_INFO` VALUES.+?;).*\\Z", Pattern.DOTALL).matcher(fileContent).replaceAll("$1");
			insertStatement = insertStatement.replaceFirst("INSERT INTO `VIDEO_INFO` VALUES \\(", "\\(");		
			insertStatement = insertStatement.substring(0, insertStatement.length()-1);		
			
			String[] insertRows = insertStatement.split("\\),\\(");
			
			for(String row : insertRows) {
				row = row.replace("\\(", "").replace("\\)", "");
				String[] insertValues = row.split("','");
				
				String name = insertValues[0].replace("'", "");
				
				Video video = new Video(name);
				videos.add(video);
			}			
		} catch (Exception e) {
			LOGGER.severe("Exception while parsing video info: " + e);
		}
	}

	private String readFile(String fileName) throws Exception {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName); 
				Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
			return scanner.hasNext() ? scanner.next() : "";
		} catch (Exception e) {
			LOGGER.severe("Exception while reading file: " + e);
			throw e;
		}
	}

	public List<Video> getVideos() {
		return videos;
	}

	/**
	 * Just for testing (set working directory to src/main/resources!!
	 */
	public static void main(String args[]) {
		try {
			DumpFileParser parser = new DumpFileParser();
			parser.parse("video_info.sql");
			for(Video video : parser.getVideos()) {
				System.out.println(video.getId());
			}
			
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
}
