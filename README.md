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

Technical documentation
------

* Google Places API:
 Service that returns information about Places (geographic locations or prominent points of interest)
* R-tree:
 R-tree for spatial queries
* [UML]
* [Frameworks]
* …

[Evaluation gegenüber Baseline]
------

Downloads
------

* [Presentation (in German on Google Drive)](https://docs.google.com/presentation/d/1lwKc9aU_un-70tZVGaXffSTX7jAdVe78plG9x1HVGo4/edit?usp=sharing)
* [Sourcecode (on Github)](https://github.com/stonerworx/mediaq-poi)

References
------

* 1: MediaQ [http://mediaq.usc.edu/][1]
* 2: Google Places API [https://developers.google.com/places/documentation/][2]

[1]: http://mediaq.usc.edu/ "MediaQ"
[2]: https://developers.google.com/places/documentation/ "Google Places API"

---

<small>*Developed as part of
[MediaQ: Practical Seminar on Big Data for Social Media](http://www.dbs.ifi.lmu.de/cms/Hauptseminar_%22MediaQ%22_WS1415)
at [Ludwig Maximilians University Munich](http://www.uni-muenchen.de/),
[Insitute of Computer Science](http://www.ifi.lmu.de/)
by David Steiner, Ramona Zeller and Johannes Ziegltrum.*</small>