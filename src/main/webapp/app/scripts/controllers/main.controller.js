(function() {

  'use strict';

  function MainController(uiGmapGoogleMapApi, dataService) {
    var vm = this;

    vm.videos = [];
    vm.activeVideo = {path: [], visible: false};

    vm.setActive = function(video) {
      vm.activeVideo = video;
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

      dataService.getVideos().then(function(videos) {
        vm.videos = [];

        for (var i in videos) {
          var video = videos[i];
          var path = [];
          var latitude = 0;
          var longitude = 0;
          var points = video.trajectory.timeStampedPoints;

          //sort by frame number
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
          video.latitude = latitude;
          video.longitude = longitude;
          video.path = path;
          video.pathStyle = {
            id: i,
            path: path,
            stroke: {
              color: '#6060FB',
              weight: 3
            },
            visible: true
          };
          video.icon = 'http://maps.google.com/mapfiles/ms/icons/red-dot.png';
          video.click = function(marker) { vm.setActive(marker.model); };
          vm.videos.push(video);
        }

      });

    });
  }

  angular.module('mediaqPoi')
    .controller('MainController', ['uiGmapGoogleMapApi', 'dataService', MainController]);

})();
