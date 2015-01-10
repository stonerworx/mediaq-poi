package de.lmu.ifi.dbs.mediaqpoi;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
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

    resp.addHeader("Access-Control-Allow-Origin", "*");
    resp.setContentType("application/json");

    Gson gson = new Gson();

    Map<String, Object> response = new HashMap<String, Object>();
    response.put("status", "ok");

    PersistenceManager pm = PMF.get().getPersistenceManager();

    String action = req.getParameter("action");

    if (action != null && action.equals("details") && req.getParameter("id") != null) {
      Key k = KeyFactory.createKey(Video.class.getSimpleName(), req.getParameter("id"));
      try {
        Video video = pm.getObjectById(Video.class, k);

        video.getTrajectory();
        video.getSearchRadius();
        video.getNearbyPois();
        video.getVisiblePois();

        response.put("video", video);
      } catch (javax.jdo.JDOObjectNotFoundException e) {
        response.put("status", "error");
        response.put("message", "Video not found.");
      }
    } else {
      Query q = pm.newQuery(Video.class);

      List<Video> videos = new ArrayList<Video>();

      try {
        List<Video> results = (List<Video>) q.execute();
        if (!results.isEmpty()) {
          for (Video video: results) {
            video.getTrajectory();
            videos.add(video);
          }
        } else {

          //TODO: remove and use real data
          Video video1 = new Video("1n940cuzuw3fi_2014_12_8_Videotake_1418038438174.mp4", "Testvideo 1");

          TrajectoryPoint tp1 = new TrajectoryPoint(1, 48.152187, 11.592492, 352.28128, -9.597204,  87.91111,  0.1,    51, 1418038438811L);
          video1.addTrajectoryPoint(tp1);

          TrajectoryPoint tp2 = new TrajectoryPoint(2, 48.152187, 11.592492, 348.31525, -11.656581, 89.825325,  0.1,    51, 1418038438814L);
          video1.addTrajectoryPoint(tp2);

          TrajectoryPoint tp3 = new TrajectoryPoint(3, 48.152187, 11.592492, 349.32913, -12.251886,  89.30554,  0.1,    51, 1418038438814L);
          video1.addTrajectoryPoint(tp3);

          TrajectoryPoint tp4 = new TrajectoryPoint(4, 48.152187, 11.592492, 352.6046, -13.812843, 88.258095,  0.1,    51, 1418038438814L);
          video1.addTrajectoryPoint(tp4);

          TrajectoryPoint tp5 = new TrajectoryPoint(5, 48.152187, 11.592492, 351.61664, -11.474263,  88.01563,  0.1,    51, 1418038438814L);
          video1.addTrajectoryPoint(tp5);

          TrajectoryPoint tp6 = new TrajectoryPoint(6, 48.152187, 11.592492, 348.57904, -13.385277,  89.00518,  0.1,    51, 1418038438814L);
          video1.addTrajectoryPoint(tp6);

          TrajectoryPoint tp7 = new TrajectoryPoint(7, 48.152187, 11.592492, 346.19858, -15.075933,  89.19742,  0.1,    51, 1418038438814L);
          video1.addTrajectoryPoint(tp7);

          TrajectoryPoint tp8 = new TrajectoryPoint(8, 48.152403, 11.59219, 347.97443, -15.249314,  85.45663,  0.1,    51, 1418038451612L);
          video1.addTrajectoryPoint(tp8);

          TrajectoryPoint tp9 = new TrajectoryPoint(9, 48.152403, 11.59219, 345.38354, -12.490302,  86.31679,  0.1,    51, 1418038451612L);
          video1.addTrajectoryPoint(tp9);

          TrajectoryPoint tp10 = new TrajectoryPoint(10, 48.152403, 11.59219, 345.81836, -11.510367,  89.53561,  0.1,    51, 1418038451612L);
          video1.addTrajectoryPoint(tp10);

          TrajectoryPoint tp11 = new TrajectoryPoint(11, 48.152403, 11.59219, 1.5293881, -16.820906,  82.52942,  0.1,    51, 1418038451612L);
          video1.addTrajectoryPoint(tp11);

          TrajectoryPoint tp12 = new TrajectoryPoint(12, 48.152403, 11.59219, 2.5045202, -6.9362087,  92.23532,  0.1,    51, 1418038451612L);
          video1.addTrajectoryPoint(tp12);

          TrajectoryPoint tp13 = new TrajectoryPoint(13, 48.152403, 11.59219, 15.032611, -5.8767695,  93.21849,  0.1,    51, 1418038451612L);
          video1.addTrajectoryPoint(tp13);

          TrajectoryPoint tp14 = new TrajectoryPoint(14, 48.152403, 11.59219, 35.903744, -6.1979914,  85.88856,  0.1,    51, 1418038451612L);
          video1.addTrajectoryPoint(tp14);

          TrajectoryPoint tp15 = new TrajectoryPoint(15, 48.152403, 11.59219, 57.900295,  -8.766266,  89.89291,  0.1,    51, 1418038452612L);
          video1.addTrajectoryPoint(tp15);

          TrajectoryPoint tp16 = new TrajectoryPoint(16, 48.152477, 11.591747, 70.55008,   2.390737,  89.59158,  0.1,    51, 1418038453613L);
          video1.addTrajectoryPoint(tp16);

          TrajectoryPoint tp17 = new TrajectoryPoint(17, 48.152477, 11.591747, 92.186195, -2.2912872,  87.64133,  0.1,    51, 1418038453613L);
          video1.addTrajectoryPoint(tp17);

          TrajectoryPoint tp18 = new TrajectoryPoint(18, 48.152477, 11.591747, 119.959274, -6.0796943,  89.54608,  0.1,    51, 1418038453613L);
          video1.addTrajectoryPoint(tp18);

          TrajectoryPoint tp19 = new TrajectoryPoint(19, 48.152477, 11.591747, 354.76465, -11.521731, 80.601326,  0.1,    51, 1418038453613L);
          video1.addTrajectoryPoint(tp19);

          TrajectoryPoint tp20 = new TrajectoryPoint(20, 48.152477, 11.591747, 323.30145, -12.656222,  89.52736,  0.1,    51, 1418038461612L);
          video1.addTrajectoryPoint(tp20);

          TrajectoryPoint tp21 = new TrajectoryPoint(21, 48.152477, 11.591747, 347.73843, -1.4339141,  91.73121,  0.1,    51, 1418038461612L);
          video1.addTrajectoryPoint(tp21);

          TrajectoryPoint tp22 = new TrajectoryPoint(22, 48.152477, 11.591747, 6.2104645,  -3.822386,  87.04369,  0.1,    51, 1418038461612L);
          video1.addTrajectoryPoint(tp22);

          TrajectoryPoint tp23 = new TrajectoryPoint(23, 48.152477, 11.591747, 18.317919,  -2.014577,  88.32025,  0.1,    51, 1418038461612L);
          video1.addTrajectoryPoint(tp23);

          TrajectoryPoint tp24 = new TrajectoryPoint(24, 48.152477, 11.591747, 37.418358, -9.7401705,    87.426,  0.1,    51, 1418038461612L);
          video1.addTrajectoryPoint(tp24);

          TrajectoryPoint tp25 = new TrajectoryPoint(25, 48.152477, 11.591747, 51.85172, -11.179051, 91.539246,  0.1,    51, 1418038461612L);
          video1.addTrajectoryPoint(tp25);

          TrajectoryPoint tp26 = new TrajectoryPoint(26, 48.152477, 11.591747, 61.355568,  0.8634349,        90,  0.1,    51, 1418038461612L);
          video1.addTrajectoryPoint(tp26);

          TrajectoryPoint tp27 = new TrajectoryPoint(27, 48.152301, 11.591794, 71.7291,  1.4771047, 87.721695,  0.1,    51, 1418038467413L);
          video1.addTrajectoryPoint(tp27);

          TrajectoryPoint tp28 = new TrajectoryPoint(28, 48.152301, 11.591794, 74.00962,  1.1116854,  92.75136,  0.1,    51, 1418038467413L);
          video1.addTrajectoryPoint(tp28);

          TrajectoryPoint tp29 = new TrajectoryPoint(29, 48.152301, 11.591794, 132.85698, -15.295082, 86.193474,  0.1,    51, 1418038467413L);
          video1.addTrajectoryPoint(tp29);

          TrajectoryPoint tp30 = new TrajectoryPoint(30, 48.152301, 11.591794, 288.69293, -13.687717,  91.23135,  0.1,    51, 1418038467413L);
          video1.addTrajectoryPoint(tp30);

          TrajectoryPoint tp31 = new TrajectoryPoint(31, 48.152301, 11.591794, 339.52197, -1.5164657,  86.17897,  0.1,    51, 1418038467413L);
          video1.addTrajectoryPoint(tp31);

          TrajectoryPoint tp32 = new TrajectoryPoint(32, 48.152301, 11.591794, 344.17014, -1.3188993, 89.057724,  0.1,    51, 1418038471413L);
          video1.addTrajectoryPoint(tp32);

          TrajectoryPoint tp33 = new TrajectoryPoint(33, 48.152301, 11.591794, 349.5732, -4.9663854,  91.14905,  0.1,    51, 1418038471413L);
          video1.addTrajectoryPoint(tp33);

          TrajectoryPoint tp34 = new TrajectoryPoint(34, 48.152301, 11.591794, 353.74518, -6.8766623,  89.59692,  0.1,    51, 1418038471413L);
          video1.addTrajectoryPoint(tp34);

          TrajectoryPoint tp35 = new TrajectoryPoint(35, 48.152301, 11.591794, 352.2595, -7.3414783,  89.71608,  0.1,    51, 1418038472413L);
          video1.addTrajectoryPoint(tp35);

          TrajectoryPoint tp36 = new TrajectoryPoint(36, 48.15212, 11.592853, 353.71088,   -7.28784,  87.59499,  0.1,    51, 1418038473613L);
          video1.addTrajectoryPoint(tp36);

          TrajectoryPoint tp37 = new TrajectoryPoint(37, 48.152301, 11.591794, 351.1022, -6.0824943,    89.087,  0.1,    51, 1418038474413L);
          video1.addTrajectoryPoint(tp37);

          TrajectoryPoint tp38 = new TrajectoryPoint(38, 48.152301, 11.591794, 350.38318,  -6.506596,  88.57926,  0.1,    51, 1418038474413L);
          video1.addTrajectoryPoint(tp38);

          TrajectoryPoint tp39 = new TrajectoryPoint(39, 48.152301, 11.591794, 353.1969, -7.4808955,  87.98944,  0.1,    51, 1418038477413L);
          video1.addTrajectoryPoint(tp39);

          TrajectoryPoint tp40 = new TrajectoryPoint(40, 48.152301, 11.591794, 351.5411,  -7.097056,  90.28308,  0.1,    51, 1418038477413L);
          video1.addTrajectoryPoint(tp40);

          TrajectoryPoint tp41 = new TrajectoryPoint(41, 48.152301, 11.591794, 350.73282,  -6.459255, 89.196304,  0.1,    51, 1418038477413L);
          video1.addTrajectoryPoint(tp41);

          TrajectoryPoint tp42 = new TrajectoryPoint(42, 48.152301, 11.591794, 350.31232, -7.5636954,  88.97798,  0.1,    51, 1418038481413L);
          video1.addTrajectoryPoint(tp42);

          TrajectoryPoint tp43 = new TrajectoryPoint(43, 48.152301, 11.591794, 350.75174,  -6.436091,  87.82598,  0.1,    51, 1418038481413L);
          video1.addTrajectoryPoint(tp43);

          TrajectoryPoint tp44 = new TrajectoryPoint(44, 48.152301, 11.591794, 349.6996,  -7.519072,  87.95257,  0.1,    51, 1418038481413L);
          video1.addTrajectoryPoint(tp44);

          TrajectoryPoint tp45 = new TrajectoryPoint(45, 48.152301, 11.591794, 350.4762, -7.0324507,  87.45006,  0.1,    51, 1418038481413L);
          video1.addTrajectoryPoint(tp45);

          TrajectoryPoint tp46 = new TrajectoryPoint(46, 48.152301, 11.591794, 349.59186, -7.1229205,  88.60282,  0.1,    51, 1418038481413L);
          video1.addTrajectoryPoint(tp46);

          TrajectoryPoint tp47 = new TrajectoryPoint(47, 48.152301, 11.591794, 348.4882, -7.3259916,  88.40093,  0.1,    51, 1418038481413L);
          video1.addTrajectoryPoint(tp47);

          TrajectoryPoint tp48 = new TrajectoryPoint(48, 48.152304, 11.591875, 349.9612, -6.6093545,  86.11949,  0.1,    51, 1418038489413L);
          video1.addTrajectoryPoint(tp48);

          TrajectoryPoint tp49 = new TrajectoryPoint(49, 48.152304, 11.591875, 349.9788, -6.8469205,  87.83893,  0.1,    51, 1418038489413L);
          video1.addTrajectoryPoint(tp49);

          TrajectoryPoint tp50 = new TrajectoryPoint(50, 48.152304, 11.591875, 351.91995, -6.3524337,   86.2521,  0.1,    51, 1418038489413L);
          video1.addTrajectoryPoint(tp50);

          TrajectoryPoint tp51 = new TrajectoryPoint(51, 48.152304, 11.591875, 349.5256,  -8.352057,  85.93989,  0.1,    51, 1418038489413L);
          video1.addTrajectoryPoint(tp51);

          TrajectoryPoint tp52 = new TrajectoryPoint(52, 48.152304, 11.591875, 348.69537,  -6.792612, 87.360985,  0.1,    51, 1418038489413L);
          video1.addTrajectoryPoint(tp52);

          TrajectoryPoint tp53 = new TrajectoryPoint(53, 48.152307, 11.591866, 349.0546, -6.2741165, 88.013336,  0.1,    51, 1418038490413L);
          video1.addTrajectoryPoint(tp53);

          TrajectoryPoint tp54 = new TrajectoryPoint(54, 48.152311, 11.591859, 347.8421,  -6.047685,  88.16716,  0.1,    51, 1418038491413L);
          video1.addTrajectoryPoint(tp54);

          TrajectoryPoint tp55 = new TrajectoryPoint(55, 48.152315, 11.591852, 350.1987, -6.2320614, 85.534645,  0.1,    51, 1418038492412L);
          video1.addTrajectoryPoint(tp55);

          TrajectoryPoint tp56 = new TrajectoryPoint(56, 48.152201, 11.592368, 337.78912,   -5.85147,  89.82207,  0.1,    51, 1418038493613L);
          video1.addTrajectoryPoint(tp56);

          TrajectoryPoint tp57 = new TrajectoryPoint(57, 48.152187, 11.592482, 335.95313,  -8.423289,  87.86395,  0.1,    51, 1418038494413L);
          video1.addTrajectoryPoint(tp57);

          TrajectoryPoint tp58 = new TrajectoryPoint(58, 48.152342, 11.591816, 334.41403, -7.1757455,  92.29061,  0.1,    51, 1418038495412L);
          video1.addTrajectoryPoint(tp58);

          TrajectoryPoint tp59 = new TrajectoryPoint(59, 48.15235, 11.591812, 343.4911, -4.4248486,  82.00547,  0.1,    51, 1418038496415L);
          video1.addTrajectoryPoint(tp59);

          TrajectoryPoint tp60 = new TrajectoryPoint(60, 48.152354, 11.591813, 325.77124, -7.1749935,  90.34901,  0.1,    51, 1418038497412L);
          video1.addTrajectoryPoint(tp60);

          TrajectoryPoint tp61 = new TrajectoryPoint(61, 48.152358, 11.591813, 336.7439,  -14.83654,  86.35028,  0.1,    51, 1418038498412L);
          video1.addTrajectoryPoint(tp61);

          TrajectoryPoint tp62 = new TrajectoryPoint(62, 48.152361, 11.591813, 15.102279, -16.746452,  95.28025,  0.1,    51, 1418038499412L);
          video1.addTrajectoryPoint(tp62);

          videos.add(video1);

          try {
            pm.makePersistent(video1);
            pm.makePersistent(tp1);
            pm.makePersistent(tp2);
            pm.makePersistent(tp3);
            pm.makePersistent(tp4);
            pm.makePersistent(tp5);
            pm.makePersistent(tp6);
            pm.makePersistent(tp7);
            pm.makePersistent(tp8);
            pm.makePersistent(tp9);
            pm.makePersistent(tp10);
            pm.makePersistent(tp11);
            pm.makePersistent(tp12);
            pm.makePersistent(tp13);
            pm.makePersistent(tp14);
            pm.makePersistent(tp15);
            pm.makePersistent(tp16);
            pm.makePersistent(tp17);
            pm.makePersistent(tp18);
            pm.makePersistent(tp19);
            pm.makePersistent(tp20);
            pm.makePersistent(tp21);
            pm.makePersistent(tp22);
            pm.makePersistent(tp23);
            pm.makePersistent(tp24);
            pm.makePersistent(tp25);
            pm.makePersistent(tp26);
            pm.makePersistent(tp27);
            pm.makePersistent(tp28);
            pm.makePersistent(tp29);
            pm.makePersistent(tp30);
            pm.makePersistent(tp31);
            pm.makePersistent(tp32);
            pm.makePersistent(tp33);
            pm.makePersistent(tp34);
            pm.makePersistent(tp35);
            pm.makePersistent(tp36);
            pm.makePersistent(tp37);
            pm.makePersistent(tp38);
            pm.makePersistent(tp39);
            pm.makePersistent(tp40);
            pm.makePersistent(tp41);
            pm.makePersistent(tp42);
            pm.makePersistent(tp43);
            pm.makePersistent(tp44);
            pm.makePersistent(tp45);
            pm.makePersistent(tp46);
            pm.makePersistent(tp47);
            pm.makePersistent(tp48);
            pm.makePersistent(tp49);
            pm.makePersistent(tp50);
            pm.makePersistent(tp51);
            pm.makePersistent(tp52);
            pm.makePersistent(tp53);
            pm.makePersistent(tp54);
            pm.makePersistent(tp55);
            pm.makePersistent(tp56);
            pm.makePersistent(tp57);
            pm.makePersistent(tp58);
            pm.makePersistent(tp59);
            pm.makePersistent(tp60);
            pm.makePersistent(tp61);
            pm.makePersistent(tp62);
          } finally {
            pm.close();
          }

        }
      } finally {
        q.closeAll();
      }

      response.put("videos", videos);
    }

    resp.getWriter().write(gson.toJson(response));
  }
}