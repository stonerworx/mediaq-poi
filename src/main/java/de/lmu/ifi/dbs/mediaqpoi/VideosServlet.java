package de.lmu.ifi.dbs.mediaqpoi;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VideosServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    resp.setContentType("application/json");

    Gson gson = new Gson();

    Map<String, Object> response = new HashMap<String, Object>();

    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query q = pm.newQuery(Video.class);

    try {
      List<Video> results = (List<Video>) q.execute();
      if (!results.isEmpty()) {
        response.put("videos", results);
      } else {
        Video video1 = new Video("Testvideo 1");
        Video video2 = new Video("Testvideo 2");
        Video video3 = new Video("Testvideo 3");

        try {
          pm.makePersistent(video1);
          pm.makePersistent(video2);
          pm.makePersistent(video3);
        } finally {
          pm.close();
        }

        response.put("info", "no results. added some testvideos. please reload.");

      }
    } finally {
      q.closeAll();
    }

    resp.getWriter().write(gson.toJson(response));
  }
}