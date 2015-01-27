(function() {

  'use strict';

  function VideoModelFactory($sce) {

    function VideoModel(video) {

      var id = false;
      var visible = false;
      var latitude = 0;
      var longitude = 0;
      var path = [];
      var center = {};
      var radius = 0;
      var nearbyPois = [];
      var visiblePois = [];
      var timeline = {};
      var posTimeline = {};
      var second = 0;

      if (video !== undefined) {
        id = video.key.name;

        //sort points by frame number
        var points = video.trajectory.timeStampedPoints;
        points.sort(function(a, b) {
          if (a.frame < b.frame) {
            return -1;
          }
          if (a.frame > b.frame) {
            return 1;
          }
          return 0;
        });

        for (var j in points) {
          var trajectoryPoint = points[j];
          path.push({
                      latitude: trajectoryPoint.latitude,
                      longitude: trajectoryPoint.longitude
                    });
          if (latitude === 0 && longitude === 0) {
            latitude = trajectoryPoint.latitude;
            longitude = trajectoryPoint.longitude;
          }
        }
      }

      var getMarkers = function(elements) {
        var markers = [];
        angular.forEach(elements, function(poi) {
          markers.push(poi.getMarker());
        });
        return markers;
      };

      this.getId = function() {
        return id;
      };

      this.show = function() {
        visible = true;
      };

      this.hide = function() {
        visible = false;
      };

      this.getVisible = function() {
        return visible;
      };

      this.getSrc = function() {
        if (!id) {
          return '';
        }
        return $sce.trustAsResourceUrl(
          'http://mediaq.dbs.ifi.lmu.de/MediaQ_MVC_V2/video_content/' + video.fileName
        );
      };

      this.getLatitude = function() {
        return latitude;
      };

      this.getLongitude = function() {
        return longitude;
      };

      this.getMarker = function() {
        return {
          id: this.getId(),
          latitude: this.getLatitude(),
          longitude: this.getLongitude(),
          icon: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png'
        };
      };

      this.setCenter = function(c) {
        center = c;
      };

      this.setRadius = function(r) {
        radius = r;
      };

      this.setNearbyPois = function(pois) {
        nearbyPois = pois;
      };

      this.setVisiblePois = function(pois) {
        visiblePois = pois;
      };

      this.setTimeline = function(t) {
        timeline = t;
      };

      this.setPosTimeline = function(pt) {
        posTimeline = pt;
      };

      this.getCenter = function() {
        return center;
      };

      this.getRadius = function() {
        return radius;
      };

      this.getZoomLevel = function() {
        return 17;
      };

      this.getPath = function() {
        return path;
      };

      this.getNearbyPois = function() {
        return nearbyPois;
      };

      this.getVisiblePois = function() {
        return visiblePois;
      };

      this.getTimeline = function() {
        return timeline;
      };

      this.getNearbyPoiMarkers = function() {
        return getMarkers(nearbyPois);
      };

      this.getVisiblePoiMarkers = function() {
        return getMarkers(visiblePois);
      };

      this.setSecond = function(s) {
        second = parseInt(s);
      };

      this.getSecond = function() {
        return second;
      };

      this.getCurrentPois = function() {
        return timeline[this.getSecond()];
      };

      this.getCurrentPoiMarkers = function() {
        return getMarkers(this.getCurrentPois());
      };

      this.getCurrentCoordinates = function() {
        var point = posTimeline[this.getSecond()];
        var latitude = null;
        var longitude = null;
        if (point !== undefined) {
          latitude = point.latitude;
          longitude = point.longitude;
        }
        return {
          latitude: latitude,
          longitude: longitude
        };
      };

      this.getCurrentRotation = function() {
        var point = posTimeline[this.getSecond()];
        if (point !== undefined) {
         return point.thetaX;
        }
        return 90;
      };

    }

    return VideoModel;

  }

  angular.module('mediaqPoi')
    .factory('VideoModel', ['$sce', VideoModelFactory]);

})();
