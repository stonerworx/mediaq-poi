(function() {

  'use strict';

  function MainController(uiGmapGoogleMapApi, dataService, VideoModel) {
    var vm = this;

    vm.markers = [];
    vm.nearbyPoiMarkers = [];
    vm.visiblePoiMarkers = [];
    vm.activeVideo = new VideoModel();
    vm.loading = false;

    //get video player
    var player = angular.element('.videoplayer video')[0];
    //listen for time updates
    player.addEventListener('timeupdate', function () {
      var second = Math.round(player.currentTime);

      vm.activeVideo.setSecond(second);

    }, false);

    vm.clickMarker = function(marker) {
      vm.openVideo(marker.model.id);
    };

    vm.closeVideo = function() {
      vm.activeVideo = new VideoModel();
      player.pause();
      vm.map.zoom = 15;
    };

    vm.openVideo = function(videoId) {
      vm.loading = true;
      dataService.getVideo(videoId).then(function(video) {
        vm.loading = false;

        vm.activeVideo = video;

        vm.nearbyPoiMarkers = vm.activeVideo.getNearbyPoiMarkers();
        vm.visiblePoiMarkers = vm.activeVideo.getVisiblePoiMarkers();

        vm.map.center = vm.activeVideo.getCenter();
        vm.map.zoom = vm.activeVideo.getZoomLevel();

        vm.activeVideo.show();

      });
    };

    uiGmapGoogleMapApi.then(function (maps) {
      vm.map = {
        center: {
          latitude: 48.150529,
          longitude: 11.595077
        },
        zoom: 15
      };
      vm.options = {
        mapTypeControl: false,
        zoomControl: true,
        zoomControlOptions: {
          style: maps.ZoomControlStyle.LARGE,
          position: maps.ControlPosition.LEFT_CENTER
        },
        panControl: false,
        streetViewControl: false
      };

      vm.loading = true;
      dataService.getVideos().then(function(videos) {
        vm.loading = false;

        angular.forEach(videos, function(video) {
          vm.markers.push(video.getMarker());
        });

      });

    });

  }

  angular.module('mediaqPoi')
    .controller('MainController', ['uiGmapGoogleMapApi', 'dataService', 'VideoModel',
                                   MainController]);

})();
