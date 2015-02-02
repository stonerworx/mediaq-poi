[MediaQ][1]<sup>1</sup> - POI
==========

* David Steiner
* Ramona Zeller
* Johannes Ziegltrum

Introduction
------

People like watching videos, especially if they show interesting things or locations - or cats.
![alt text](/images/documentation/cat.gif)

With [MediaQ][1]<sup>1</sup> we will soon have the opportunity to watch videos from all
over the world. The problem is that we don’t know what a video shows before we have watched it.
It would be much easier for us if we knew beforehand whether the video is showing the place we're
looking for or not. Additionally it would be handy if we could just search for a place we're
interested in and get a list of videos that have captured it.

Introducing **Trajectory-To-POI-Mapping** - connecting videos to their underlying geographical
information.

This way we have the opportunity to search for certain *points of interest* (POI) like sights, parks
or restaurants and are able to tell exactly when an interesting place is visible in a video.

Example use-cases could be:

* Someone is telling you about an amazing place you have never been before. No Problem. Just search
for all videos that show exactly this place.
* People who travel the world can get an impression of what to expect at certain destinations and
check out interesting locations before they get there.
* People who can't travel the world can see the greatest places on earth through the eyes of others.

On the one hand we have the video database of [MediaQ][1]<sup>1</sup>, containing videos and their
meta-information like trajectories, trajectory points or the perspective. On the other hand we have
services like the [Google Places API][2]<sup>2</sup> that returns information about places
(establishments, geographic locations, or prominent points of interest). The idea is to connect the
information of both systems to a single, even more powerful, application.

---

Our approach
------

#### 1) Find all videos that show a given POI

* Determine the coordinates (latitude, longitude) of the POI via [Google Places API][2]<sup>2</sup>

##### Filtering with R-tree

* R-tree contains all videos as minimal bounded rectangles (MBRs) -> trajectory of a video can simplified be shown as a MBR

![alt text](/images/documentation/trajectory_mbr.png "trajectory mbr")

* POI with visibility range can also be shown as a MBR

![alt text](/images/documentation/poi_mbr.png "poi mbr")

* R-tree range query with the POI-MBR

![alt text](/images/documentation/rtree_range_query.png "rtree range query")

-> Results in video candidates

##### Refinement
For each video candidate determine if the POI is visible in a trajectory point of the video:

* What we know
 
![alt text](/images/documentation/thetax.png "thetax") ![alt text](/images/documentation/alpha_radius.png "alpha & radius")
 
    Foreach video in candidates
      trajectory = video.getTrajectory();
      Foreach trajectory_point in trajectory
        calculate the distance and the angle between the trajectory_point and the given POI
        if (distance <= radius) AND (thetax - alpha/2 <= angle <= thetax + alpha/2)
          -> add video to result set

![alt text](/images/documentation/angle_distance.png "angle & distance")

-> Result set with all videos that show the given POI

#### 2) Find all POIs that are shown in a given video

* Trajectory of a video can simplified be shown as a MBR

![alt text](/images/documentation/trajectory_mbr.png "trajectory mbr")

##### Filtering with Google Places API

* Get all POIs located in the video-MBR
 
-> Results in POI candidates

##### Refinement
For each trajectory point of the video determine which of the POI candidates is visible:

* What we know

![alt text](/images/documentation/thetax.png "thetax") ![alt text](/images/documentation/alpha_radius.png "alpha & radius")
 
    trajectory = video.getTrajectory();
    Foreach trajectory_point in trajectory
      Foreach POI in candidates
        calculate the distance and the angle between the trajectory_point and the POI
        if (distance <= radius) AND (thetax - alpha/2 <= angle <= thetax + alpha/2)
          -> add POI to result set

![alt text](/images/documentation/angle_distance.png "angle & distance")

-> Result set with all visible POIs

Backend
------

####Get all videos within certain bounds:

Request:

    http://mediaq-poi.appspot.com/videos?
    action=range_query&
    bound1_lat=48.158445160282696&bound1_lng=11.624752960540718&
    bound2_lat=48.142611618392216&bound2_lng=11.565401039459175

Parameters: North-East and South-West Latitude and Longitude.

Returns: A list of videos.

Response:

    {
      status: "ok",
      videos: [
        {
          fileName: "3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
          filePath: "http://mediaq.dbs.ifi.lmu.de/MediaQ_MVC_V2/video_content/3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
          id: "3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
          latitude: 48.152392,
          longitude: 11.591949
        },
        {
          fileName: "-z0waj48xtiyf_2014_12_8_Videotake_1418038301552.mp4",
          filePath: "http://mediaq.dbs.ifi.lmu.de/MediaQ_MVC_V2/video_content/-z0waj48xtiyf_2014_12_8_Videotake_1418038301552.mp4",
          id: "-z0waj48xtiyf_2014_12_8_Videotake_1418038301552.mp4",
          latitude: 48.152059,
          longitude: 11.592511
        },
        ...
      ]
    }

####Get all POIs for a video

Request:

    http://mediaq-poi.appspot.com/video/3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4

Parameters: The video id.

Returns:

* The video details
* The calculated center of the videos trajectory
* The radius enclosing the trajectory that has been used for the range query on [Google Places API][2]<sup>2</sup>
* A list of nearby POIs - the candidates received from [Google Places API][2]<sup>2</sup>
* A list of visible POIs - calculated by our algorithm
* A list of user positions with latitude, longitude and viewing direction sorted by position in the video (in seconds)
* The timeline sorted by position in the video (in seconds) describing what POI is visible at this time

Response:

    {
      center: {
        latitude: 48.1523895,
        longitude: 11.592268
      },
      searchRange: 150,
      visiblePois: [
        {
          reference: "CpQBgwAAAOYnaYrxXr71LZvu-LQXNkn9C2y7FJ3PzsubGEYLZyMziA5xhofRcD3qo60c-5uHEyVySlGsfLu8BlMqJ3SJYnIEv5Q9ft-73vemoVO_KQGltSTQ3J87vqRL_UlcmhGPLI7hGoa-2LiS6MLBaQGABWY577Gls81p8RHY91Eti8hYjbjc5sHkW7IapKn6ndpriBIQWVZZFJr0vDokKevMuBxZcxoUcWLpwYNfNSH_O9puzuwRWJZujSk",
          id: "ChIJSynAOJZ1nkcRLbbGZPaCR1o",
          latitude: 48.152005,
          longitude: 11.592346,
          name: "Christmas Market at Chinese Tower"
        },
        ...
      ],
      nearbyPois: [
        {
          reference: "CoQBegAAAPnqBhtWfnaD_ib0RkV-znyJyWpQiN9rGonzjQgKLoSplnH-q7WMSLWIvvAHihFTIGSH-4bMpQqraGZQVu2MXRZqhdwJAzUXPUIxNMm-TgaQupimLuyCZ9HoN_P2u7P0eFm7i8II4ZXY_iLSVjsgXA8JuK9UISOEWeZKDmVnpC4cEhDtevrh-YUOq-Vg4SXKA8RMGhT2Rd8n2o-U86lQNdZqacDVCICbRg",
          id: "ChIJayv4lZd1nkcR0e_vfGLfm8k",
          latitude: 48.16423229999999,
          longitude: 11.6055522,
          name: "English Garden"
        },
        ...
      ],
      posTimeline: {
        0: {
          latitude: 48.152392,
          longitude: 11.591949,
          thetaX: 259.97482
        },
        ...
      },
      timeline: {
        0: [ ],
        1: [ ],
        2: [
          {
            reference: "CpQBgwAAAOYnaYrxXr71LZvu-LQXNkn9C2y7FJ3PzsubGEYLZyMziA5xhofRcD3qo60c-5uHEyVySlGsfLu8BlMqJ3SJYnIEv5Q9ft-73vemoVO_KQGltSTQ3J87vqRL_UlcmhGPLI7hGoa-2LiS6MLBaQGABWY577Gls81p8RHY91Eti8hYjbjc5sHkW7IapKn6ndpriBIQWVZZFJr0vDokKevMuBxZcxoUcWLpwYNfNSH_O9puzuwRWJZujSk",
            id: "ChIJSynAOJZ1nkcRLbbGZPaCR1o",
            latitude: 48.152005,
            longitude: 11.592346,
            name: "Christmas Market at Chinese Tower"
          },
          {
            reference: "CoQBewAAAJif1b-ypdFUx1xCd2HMd9yU6Kjih1aJ71j_frWYGXHhCgS17EH2TLxT6cf3ZNnSsvzGGoV6P4XKPed9rTXs4TVj6eVR4uzVmjK805ZKwAbVGbXDeAM_JnGX-qePrsKJrD9EOlZ1OtNlQF22P7L48AbOqy5vnDlKaQQ4V5fo4mBkEhAPeH6opDtO2hQqxjCATZ7QGhTB-SMddeyJc47Y4SpkMrZKM9y-mQ",
            id: "ChIJx8O3wZd1nkcRSD11Hq3mOm0",
            latitude: 48.152005,
            longitude: 11.592346,
            name: "Ochsenbraterei Haberl OHG"
          }
        ],
        ...
      },
      video: {
        fileName: "3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
        filePath: "http://mediaq.dbs.ifi.lmu.de/MediaQ_MVC_V2/video_content/3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
        id: "3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
        latitude: 48.152392,
        longitude: 11.591949
      }
    }

####Get all videos for a POI

Request:

    http://mediaq-poi.appspot.com/poi/ChIJ6Q6XOph1nkcRQWtXFc8qRRg

Parameters: The POI id.

Returns: The POI details and a list of videos that show the POI.

Response:

    {
      poi: {
        reference: "CnRqAAAAd46UWaGluNFcTOmF4ex9QbKO6xE52f6fI7RU3jD69KckanXnp-CtKxi7R0YagbEs-GC99Xbc-Xpe4wOgGY1P2Rq_2fWZkTA76o8auxRHF7XjiJuDnnqd9FdUi5fgZmSgsbJ4t8BOFfywXmwaw8IozBIQNWIvBRpugSfnEJ2g7IR_sBoUkt2fWdwj_XP3P7TiQpTkhondwP0",
        id: "ChIJ6Q6XOph1nkcRQWtXFc8qRRg",
        latitude: 48.152556,
        longitude: 11.592084,
        name: "Chinesischer Turm"
      },
      videos: [
        {
          fileName: "3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
          filePath: "http://mediaq.dbs.ifi.lmu.de/MediaQ_MVC_V2/video_content/3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
          id: "3h5yxvywo2kg_2014_12_8_Videotake_1418038378126.mp4",
          latitude: 48.152392,
          longitude: 11.591949
        },
        {
          fileName: "3h5yxvywo2kg_2014_12_8_Videotake_1418037868170.mp4",
          filePath: "http://mediaq.dbs.ifi.lmu.de/MediaQ_MVC_V2/video_content/3h5yxvywo2kg_2014_12_8_Videotake_1418037868170.mp4",
          id: "3h5yxvywo2kg_2014_12_8_Videotake_1418037868170.mp4",
          latitude: 48.152499,
          longitude: 11.592498
        },
        ..
      ]
    }

Frontend
------

To demonstrate the abilities of our implementation we developed a sample application that queries
our interface.

You can check it out [here](http://mediaq-poi.appspot.com).

The application features a map that displays all videos in the visible bounds. If you click on a
video-marker the video details are loaded. Once this is done the video is played and the trajectory
shown. While the video is playing the current location and the viewing direction as well as the
visible POIs at the current time keep being updated and displayed on the map.

It is also possible to search for a POI using the search field in the header. The search is using
the autocomplete function provided by the [Google Places API][2]<sup>2</sup>. When a POI is
selected it's details are loaded and the POI and the videos that show it are displayed on the map.

Technical documentation
------

* [Google Places API][2]<sup>2</sup>

  Used for getting a list of POIs in a given range, the canditates for our algorithm, and
  searching for POIs.

* [Google App Engine][3]<sup>3</sup>

  ![alt text](/images/documentation/appengine-logo.png "Google App Engine")

  The frontend and backend are hosted on [Google App Engine][3]<sup>3</sup>. App Engine is a cloud
  service for hosting Java, Python, PHP or Go applications. It also features "a schemaless NoSQL
  datastore providing robust, scalable storage" which we use for persisting the videos, trajectories
  and trajectory points.

* [AngularJS][4]<sup>4</sup>

  ![alt text](/images/documentation/AngularJS-small.png "AngularJS")

  Angular is a framework for JavaScript with features like two-way data-binding and dependency
  injection that allowed us to write a well structured frontend application.

* [GruntJS][5]<sup>5</sup>

  ![alt text](/images/documentation/grunt-logo.png "GruntJS")

  [Grunt][5]<sup>5</sup>, The JavaScript Task Runner is automatically building our frontend
  distribution - including JavaScript and image minification and much more.

* R-tree:
 R-tree for spatial queries
* [UML]
* …

Performance Evaluation
------

TODO

Downloads
------

* [Presentation (in German on Google Drive)](https://docs.google.com/presentation/d/1lwKc9aU_un-70tZVGaXffSTX7jAdVe78plG9x1HVGo4/edit?usp=sharing)
* [Sourcecode (on Github)](https://github.com/stonerworx/mediaq-poi)

References
------

* 1: MediaQ [http://mediaq.usc.edu/][1]
* 2: Google Places API [https://developers.google.com/places/documentation/][2]
* 3: Google App Engine [https://cloud.google.com/appengine/docs][3]
* 4: AngularJS [https://angularjs.org/][4]
* 5: GruntJS [http://gruntjs.com/][5]

[1]: http://mediaq.usc.edu/ "MediaQ"
[2]: https://developers.google.com/places/documentation/ "Google Places API"
[3]: https://cloud.google.com/appengine/docs "Google App Engine"
[4]: https://angularjs.org/ "AngularJS"
[5]: http://gruntjs.com/ "GruntJS"

---

<small>*Developed as part of
[MediaQ: Practical Seminar on Big Data for Social Media](http://www.dbs.ifi.lmu.de/cms/Hauptseminar_%22MediaQ%22_WS1415)
at [Ludwig Maximilians University Munich](http://www.uni-muenchen.de/),
[Insitute of Computer Science](http://www.ifi.lmu.de/)
by David Steiner, Ramona Zeller and Johannes Ziegltrum.*</small>