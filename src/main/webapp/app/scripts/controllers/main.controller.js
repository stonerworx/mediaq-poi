(function() {

  'use strict';

  function MainController(uiGmapGoogleMapApi, dataService, VideoModel, $scope, $timeout) {
    var vm = this;

    var delay = 3000;

    var markers = [];

    uiGmapGoogleMapApi.then(function (maps) {
      vm.loading = true;

      //get map
      var map = new maps.Map(angular.element('.angular-google-map-container')[0]);

      //get video player
      var player = angular.element('.videoplayer video')[0];

      //get autocomplete
      var autocomplete = new maps.places.Autocomplete(angular.element('#placesearch')[0]);

      vm.map = {
        control: {},
        center: {
          latitude: 48.150529,
          longitude: 11.595077
        },
        dragging: false,
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

      vm.markers = markers;
      vm.videoVisible = false;
      vm.nearbyPoiMarkers = [];
      vm.visiblePoiMarkers = [];
      vm.markerOptions = {
        animation: maps.Animation.DROP
      };
      vm.activeVideo = new VideoModel();
      vm.loading = false;
      vm.videoPositionMarker = {
        id: 'videoPosition',
        icon: {
          url: '/images/ignore/position.png',
          anchor: {
            x: 12.5,
            y: 12.5
          }
        },
        coords: {},
        control: {}
      };
      vm.videoPositionView = {
        id: 'videoView',
        icon: {
          path: 'M 0 0 L -35 -100 L 35 -100 z',
          fillColor: '#3884ff',
          fillOpacity: 0.7,
          scale: 1,
          strokeColor: '#356cde',
          rotation: 90,
          strokeWeight: 1
        }
      };
      vm.currentPoiMarkers = [];
      vm.searchRadius = {};

      vm.places = {
        autocomplete: '',
        options: null,
        results: '',
        details: ''
      };

      //listen for time updates
      player.addEventListener('timeupdate', function () {
        var second = Math.round(player.currentTime);

        vm.activeVideo.setSecond(second);
        if (vm.activeVideo.getVisible()) {
          vm.videoPositionMarker.coords = vm.activeVideo.getCurrentCoordinates();
          vm.videoPositionView.icon.rotation = vm.activeVideo.getCurrentRotation();

          vm.currentPoiMarkers = vm.activeVideo.getCurrentPoiMarkers();
          for (var i in vm.currentPoiMarkers) {
            vm.currentPoiMarkers[i].show = true;
          }
        } else {
          vm.videoPositionMarker = {};
          vm.currentPoiMarkers = [];
        }
        $scope.$apply();

      }, false);

      vm.clickMarker = function(marker) {
        vm.openVideo(marker.model.id);
      };

      vm.closeVideo = function() {
        player.pause();
        vm.activeVideo.hide();
        vm.markers = markers;
        vm.activeVideo = new VideoModel();
        vm.nearbyPoiMarkers = [];
        vm.visiblePoiMarkers = [];
        vm.videoPositionMarker.coords = {};
        vm.videoVisible = false;
        vm.currentPoiMarkers = [];
        vm.map.zoom = 15;
      };

      vm.openVideo = function(videoId) {
        vm.loading = true;
        vm.markers = [];
        dataService.getVideo(videoId).then(function(video) {

          vm.activeVideo = video;

          vm.map.center = vm.activeVideo.getCenter();
          vm.map.zoom = vm.activeVideo.getZoomLevel();

          vm.videoVisible = true;

          vm.searchRadius.center = vm.activeVideo.getCenter();

          var setCircleRadius = function(radius) {
            vm.searchRadius.radius = radius;
            if (radius < vm.activeVideo.getRadius()) {
              $timeout(function() {
                setCircleRadius(vm.searchRadius.radius+2);
              }, 5);
            } else {
              $timeout(function() {

                vm.nearbyPoiMarkers = vm.activeVideo.getNearbyPoiMarkers();

                $timeout(function() {

                  vm.nearbyPoiMarkers = [];
                  vm.visiblePoiMarkers = vm.activeVideo.getVisiblePoiMarkers();
                  for (var i in vm.visiblePoiMarkers) {
                    vm.visiblePoiMarkers[i].show = true;
                  }

                  $timeout(function() {

                    vm.searchRadius = {center:{latitude:0,longitude:0},radius:0};
                    vm.visiblePoiMarkers = [];
                    vm.activeVideo.show();
                    player.play();

                    vm.loading = false;

                  }, delay);

                }, 2/3*delay);

              }, 1/3*delay);
            }
          };

          setCircleRadius(1);

        });
      };

      //wait until the map is loaded
      maps.event.addListenerOnce(map, 'idle', function(){

        var gmap = vm.map.control.getGMap();

        navigator.geolocation.getCurrentPosition(function(position) {
          vm.map.center.latitude = position.coords.latitude;
          vm.map.center.longitude = position.coords.longitude;
          $scope.$apply();
          getVideosByBounds();
        });

        var getVideosByBounds = function() {
          var bounds = {
            northEast: {
              latitude: gmap.getBounds().getNorthEast().lat(),
              longitude: gmap.getBounds().getNorthEast().lng()
            },
            southWest: {
              latitude: gmap.getBounds().getSouthWest().lat(),
              longitude: gmap.getBounds().getSouthWest().lng()
            }
          };

          dataService.getVideos(bounds).then(function(videos) {
            vm.loading = false;

            angular.forEach(videos, function(video) {
              markers.push(video.getMarker());
            });

          });
        };

        var mapupdater;

        function mapSettleTime() {
          clearTimeout(mapupdater);
          if (!vm.videoVisible) {
            mapupdater = setTimeout(getVideosByBounds, 1000);
          }
        }

        maps.event.addListener(gmap, 'dragstart', function() {
          clearTimeout(mapupdater);
        });
        maps.event.addListener(gmap, 'dragend', mapSettleTime);
        maps.event.addListener(gmap, 'zoom_changed', mapSettleTime);

        maps.event.addListener(autocomplete, 'place_changed', function() {
          var place = autocomplete.getPlace();
          if (!place.geometry) {
            return;
          }

          vm.map.center.latitude = place.geometry.location.lat();
          vm.map.center.longitude = place.geometry.location.lng();
          vm.map.zoom = 15;
          $scope.$apply();

          getVideosByBounds();
        });

        getVideosByBounds();

      });

    });

  }

  angular.module('mediaqPoi')
    .controller('MainController', ['uiGmapGoogleMapApi', 'dataService', 'VideoModel', '$scope',
                                   '$timeout', MainController]);

})();
