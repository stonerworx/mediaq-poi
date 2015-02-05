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

Queries and algorithms
------

#### 1) Find all videos that show a given POI

##### Naive approach

    Foreach video in ALL videos
      trajectory = video.getTrajectory();
      Foreach trajectory_point in trajectory
        if POI is visible at the trajectory_point
          -> add video to result set

##### "Our" approach using the R-tree index structure

Basis of this query is a [R-tree][7]<sup>7</sup> filled with all videos of the MediaQ database. In order to do this we calculate for each video the minimum bounded rectangel (MBR) of the video trajectory and insert it into the [R-tree][7]<sup>7</sup>.

![alt text](/images/documentation/trajectory_mbr.png "trajectory mbr")

This way we can search now for videos in a certain range on the map.

###### Filtering with [R-tree][7]<sup>7</sup>

First of all we determine the coordinates (latitude, longitude) of the POI via [Google Places API][2]<sup>2</sup>. By means of this coordinates and the visibility range we define our search area.

![alt text](/images/documentation/poi_mbr.png "poi mbr")

With the MBR of this search area we can run a range query on the [R-tree][7]<sup>7</sup>. The result set of this query contains all videos which might be showing the given POI.

![alt text](/images/documentation/rtree_range_query.png "rtree range query")

###### Refinement

In the filtering phase we determined the video candidates. Now we have to check for each candidate if the POI is visible in any trajectory point of this video. Therefore we calculate the distance and the angle between each trajectory point of a video and the given POI.

![alt text](/images/documentation/angle_distance_poi.png "angle & distance")

Each trajectory point contains the information about its perspective.

![alt text](/images/documentation/trajectory_point_perspective.png "trajectory point perspective")

After comparing this values we know exactly if a video is showing the POI we are searching for.
 
    Foreach video in candidates
      trajectory = video.getTrajectory();
      Foreach trajectory_point in trajectory
        calculate the distance and the angle between the trajectory_point and the given POI
        if (distance <= radius) AND (thetax - alpha/2 <= angle <= thetax + alpha/2)
          -> add video to result set

##### "Our" approach using a Google Document Index

In this approach we use the [Google Search API][3]<sup>3</sup> which already provides geospatial index structures.
However, we have to store the videos as documents in order to use this features. We store the circular approximation of
each video trajectory as document and trust Google enough to index them efficiently ;-)
The middle point of the circle is saved as geopoint field ("centerPoint") and the search radius ("searchRange")
of the trajectory is stored as number field.

The query is solved using a filter step. The maximum search radius of the indexed trajectories is used as upper bound.
All videos that have a less or equal distance to the poi are handled as candidates:

    String queryString = "distance(centerPoint, " + geoPoint(poiLocation) + ") <= " + maxSearchRange;
    Build query with the queryString
    Execute the query and add results to candidates list

The refinement step is analogous to the R-tree refinement.

While realizing this query with the [Google Search API][3]<sup>3</sup> we faced some issues with it:

* Only very basic queries are supported by the API (see also http://stackoverflow.com/questions/28213499/is-it-possible-to-use-a-document-field-as-radius-with-distance-function-in-googl)
* Data is not stored in the data store but as document
* Some functions cannot be used in the local development server (see also https://cloud.google.com/appengine/docs/java/search/devserver)
* You have no insight into the index structure


#### 2) Find all POIs that are shown in a given video

The naive approach here would be to iterate through all trajectory points of the video and check ALL POIs
 if they are visible at each point. Of course, this is not very practical and hard to realize. Maybe Chuck Norris is the
 only one to get all POIs of the whole world in one single request from the [Google Search API][3]<sup>3</sup> ;-)

As Chuck Norris was unfortunately not willing to participate in our project, we had to find another way to solve this query.

##### Filtering with [Google Places API][2]<sup>2</sup>

First of all we calculate the circle that surrounds the trajectory of the given video and add the visibility range. [Google Places API][2]<sup>2</sup> now provides us with the POIs in this search area.

![alt text](/images/documentation/search_area.png "search area")
 
-> Results in POI candidates which might be shown in the video

##### Refinement

For each trajectory point of the video determine which of the POI candidates is visible. Again we know the perspective of each trajectory point and calculate the distance and the angle to each POI candidate. After comparing this values we get the result set with all visible POIs in this video.

![alt text](/images/documentation/trajectory_point_perspective.png "trajectory point perspective")

![alt text](/images/documentation/angle_distance_poi.png "angle & distance")
 
    trajectory = video.getTrajectory();
    Foreach trajectory_point in trajectory
      Foreach POI in candidates
        calculate the distance and the angle between the trajectory_point and the POI
        if (distance <= radius) AND (thetax - alpha/2 <= angle <= thetax + alpha/2)
          -> add POI to result set

#### 3) Find all videos that are recorded in a certain map section

To reduce the data load and achieve a good performance we decided to load only those videos that are located in the
current map section that the user has navigated to.

##### Naive approach

    foreach video in ALL videos
      trajectory = video.getTrajectory()
      get minimal bounding rectangle (MBR) of trajectory
      if MBR intersects with query window (range)
        foreach trajectory_point in trajectory
           if range contains trajectory_point
             -> add video to result set
             break;


##### "Our" approach using the R-tree index structure

The R-tree can be used directly to find videos in a certain area.

##### "Our" approach using a Google Document Index

We additionally store the minimal bounding rectangle (MBR) of the video trajectory in the document. The MBR is represented
by two opposite corner points. The location with the minimum longitude and minimum latitude is stored as geopoint field
("minPoint") and the location with the maximum coordinates finds oneself also as geopoint field ("maxPoint") in the
document.

In the refinement step, all videos that have either their minimum point or their maximum point lying in the circumcircle
of the range are loaded as candidates. The circumcircle of the range has to be used because the
[Google Search API][3]<sup>3</sup> does not support queries for intersections with geometrical figures. It only lets you
query for distances between geopoints.


    middle = center of circumcircle around rectangular range
    radius = distance between middle and one corner point
    String queryString = "distance(minPoint, " + geoPoint(middle) + ") <= " + radius;
         queryString += " OR distance(maxPoint, " + geoPoint(middle) + ") <= " + radius;
    Build query with the queryString
    Execute the query and add results to candidates list

In the refinement step the candidates are checked if they have at least one trajectory point that is located in the range
area:

    foreach video in candidates
      trajectory = video.getTrajectory()
      get minimal bounding rectangle (MBR) of trajectory (minPoint, maxPoint)
      if MBR intersects with query window (range)
        foreach trajectory_point in trajectory
           if range contains trajectory_point
             -> add video to result set
             break;

For the intersection test of the range with the MBR of the trajectory the [LatLonRect][8]<sup>8</sup> class of the
unidataCommon library is used.


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

  Used for getting a list of POIs in a given range, the candidates for our algorithm, and
  searching for POIs.

* [Google App Engine][3]<sup>3</sup>

  ![Google App Engine logo](/images/documentation/appengine-logo.png "Google App Engine")

  The frontend and backend are hosted on [Google App Engine][3]<sup>3</sup>. App Engine is a cloud
  service for hosting Java, Python, PHP or Go applications. It also features "a schemaless NoSQL
  datastore providing robust, scalable storage" which we use for persisting the videos, trajectories
  and trajectory points.

* [Google Search API][4]<sup>4</sup>

  Used for indexing the videos by their circular approximation and for getting the candidate videos
  for a given POI.

* [AngularJS][5]<sup>5</sup>

  ![AngularJS logo](/images/documentation/AngularJS-small.png "AngularJS")

  Angular is a framework for JavaScript with features like two-way data-binding and dependency
  injection that allowed us to write a well structured frontend application.

* [GruntJS][6]<sup>6</sup>

  ![GruntJS logo](/images/documentation/grunt-logo.png "GruntJS")

  [Grunt][6]<sup>6</sup>, The JavaScript Task Runner is automatically building our frontend
  distribution - including JavaScript and image minification and much more.

* [R-tree][7]<sup>7</sup>

 A high performance Java version of the [R-tree][7]<sup>7</sup> spatial indexing algorithm.
 
* [UML]

  ![UML use case diagram](/images/documentation/uml_use_cases.png "Use cases")

  Use cases of the mediaq-poi application

  ![UML sequence diagram for query "get videos for POI"](/images/documentation/uml_sequence_videos_for_poi.png "Sequence diagram")

  Sequence diagram to show how the query "get videos for POI" is solved. The servlet receives the request with the POI's
  place id as parameter. It hands over the place Id to the PoiService which retrieves the place details via the Google
  Places Api and returns the according Poi object. The servlet then can call the PoiService again to retrieve the videos
  that record the geo position of the POI. Depending on the set algorithm approach type (RTREE, GOOGLE_DOCUMENT_INDEX or NAIVE)
  the PoiService instance gets the video results and returns them to the servlet. Finally, the servlet puts the video data
  as JSON into the http response and returns it to the frontend where the data will be further handled and shown to the user.

  ![UML sequence diagram for query "get POIs for video"](/images/documentation/uml_sequence_pois_for_video.png "Sequence diagram")

  Sequence diagram to show how the query "get POIs for video" is solved. The servlet receives the request with the video's
  id as parameter. It loads the Video object from the datastore (not shown in sequence diagram) and calls the
  getVisiblePois()-method of the PoiService. The service now gets the circular approximation of the video's trajectory
  and requests the [Google Places API][2]<sup>2</sup> to return all places within this circular range. All this places are handled as candidates.
  In the refinement step all trajectory points are iterated and for every single point all candidate POIs are checked
  if they are visible at the point, If yes, they are added to the result lists. In the end, the results are returned to
  the servlet which puts them together with other information about the video to the http response (as JSON).
  The frontend can then display the information.

  ![UML class diagram](/images/documentation/uml_classes.png "Class diagram")

  Simplified class diagram of mediaq-poi application. Only the main relations are shown and the entity classes are not
  specified in detail. The servlets mainly communicate with the IPoiService interface so that the business logic
  is capsulized. The entity classes are very simple data containers that are used for communication between the
  different components.

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
* 4: Google Search Api [https://cloud.google.com/appengine/docs/java/search/][4]
* 5: AngularJS [https://angularjs.org/][5]
* 6: GruntJS [http://gruntjs.com/][6]
* 7: R-tree [https://github.com/aled/jsi/][7]
* 8: LatLonRect [http://www.unidata.ucar.edu/software/thredds/v4.3/netcdf-java/v4.3/javadoc/ucar/unidata/geoloc/LatLonRect.html][8]

[1]: http://mediaq.usc.edu/ "MediaQ"
[2]: https://developers.google.com/places/documentation/ "Google Places API"
[3]: https://cloud.google.com/appengine/docs "Google App Engine"
[4]: https://cloud.google.com/appengine/docs/java/search/ "Google Search API"
[5]: https://angularjs.org/ "AngularJS"
[6]: http://gruntjs.com/ "GruntJS"
[7]: https://github.com/aled/jsi/ "R-tree"
[8]: http://www.unidata.ucar.edu/software/thredds/v4.3/netcdf-java/v4.3/javadoc/ucar/unidata/geoloc/LatLonRect.html

---

<small>*Developed as part of
[MediaQ: Practical Seminar on Big Data for Social Media](http://www.dbs.ifi.lmu.de/cms/Hauptseminar_%22MediaQ%22_WS1415)
at [Ludwig Maximilians University Munich](http://www.uni-muenchen.de/),
[Insitute of Computer Science](http://www.ifi.lmu.de/)
by David Steiner, Ramona Zeller and Johannes Ziegltrum.*</small>