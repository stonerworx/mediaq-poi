package de.lmu.ifi.dbs.mediaqpoi.boundary;

import com.google.gson.Gson;

import de.lmu.ifi.dbs.mediaqpoi.control.PoiService;
import de.lmu.ifi.dbs.mediaqpoi.entity.Poi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PoiServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PoiServlet.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json; charset=UTF-8");

        Gson gson = new Gson();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "ok");

        String id = req.getParameter("id");

        getPoiDetails(id, responseData);

        resp.getWriter().write(gson.toJson(responseData));
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
            response.put("videos", PoiService.getInstance().getVideos(poi.getLongitude(),
                                                                      poi.getLatitude()));

        } catch (Exception e) {
            LOGGER.severe(
                String.format("Exception occurred while getting poi with id %s: %s", placeid, e));
            response.put("status", "error");
            response.put("message", "Poi not found.");
        }
    }

}
