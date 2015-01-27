(function() {

  'use strict';

  function PoiModelFactory() {

    function PoiModel(poi) {

      var id = poi.id;

      var name = poi.name;
      var latitude = poi.latitude;
      var longitude = poi.longitude;
      var videos = [];

      this.getId = function() {
        return id;
      };

      this.getName = function() {
        return name;
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
          icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
          latitude: this.getLatitude(),
          longitude: this.getLongitude(),
          options: { title: this.getName() }
        };
      };

      this.setVideos = function(videoList) {
        videos = videoList;
      };

      this.getVideos = function() {
        return videos;
      };

      this.getVideoMarkers = function() {
        var markers = [];
        angular.forEach(videos, function(video) {
          markers.push(video.getMarker());
        });
        return markers;
      };
    }

    return PoiModel;

  }

  angular.module('mediaqPoi')
    .factory('PoiModel', [PoiModelFactory]);

})();
