(function() {

  'use strict';

  function VideoModelFactory($sce) {

    function VideoModel(video) {

      var id = false;
      var filePath;
      var visible = false;
      var latitude = 0;
      var longitude = 0;
      var center = {};
      var radius = 0;
      var path = [];
      var nearbyPois = [];
      var visiblePois = [];
      var timeline = {};
      var posTimeline = {};
      var second = 0;

      if (video !== undefined) {
        id = video.id;

        filePath = video.filePath;

        latitude = video.latitude;
        longitude = video.longitude;
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
          filePath
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

      this.getPath = function() {
        return path;
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
        angular.forEach(pt, function(pos) {
          path.push({latitude: pos.latitude, longitude: pos.longitude});
        });
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
