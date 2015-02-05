package de.lmu.ifi.dbs.mediaqpoi.boundary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.lmu.ifi.dbs.mediaqpoi.control.GooglePlacesApi;
import de.lmu.ifi.dbs.mediaqpoi.control.PoiService;
import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.PlacesList;
import de.lmu.ifi.dbs.mediaqpoi.entity.Poi;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public class PoiServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PoiServlet.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json; charset=UTF-8");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "ok");

        String id = req.getParameter("id");
        String lat = req.getParameter("lat");
        String lng = req.getParameter("lng");

        if (id != null) {
          getPoiDetails(id, responseData);
        } else {
          getPoiDetailsByLocation(lat, lng, responseData);
        }

        Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

        resp.getWriter().write(gson.toJson(responseData));
    }

    private void getPoiDetailsByLocation(String lat, String lng, Map<String, Object> response) {
        Location location = new Location(Double.valueOf(lat), Double.valueOf(lng));
        PlacesList places = GooglePlacesApi.searchPlaces(location, 2);
        if (!places.results.isEmpty()) {
            getPoiDetails(places.results.get(0).placeId, response);
        }
    }

    private void getPoiDetails(String placeid, Map<String, Object> response) {
        try {
            if (placeid == null) {
                throw new IllegalArgumentException("id must not be null");
            }
            LOGGER.info("Getting details for poi id " + placeid);

            Poi poi = PoiService.getInstance().getPoi(placeid);

            if (poi == null) {
                throw new IllegalArgumentException("Poi not found.");
            }

            response.put("poi", poi);

            List<Video> videos = PoiService.getInstance().getVideos(poi.getLongitude(),
                    poi.getLatitude());

            for(Video video : videos) {
                video.touch();
            }

            response.put("videos", videos);

        } catch (Exception e) {
            LOGGER.severe(
                String.format("Exception occurred while getting poi with id %s: %s", placeid, e));
            response.put("status", "error");
            response.put("message", "Poi not found.");
        }
    }

}
