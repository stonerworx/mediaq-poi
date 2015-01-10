'use strict';

/**
 * @ngdoc function
 * @name mediaqPoi.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of mediaqPoi
 */
angular.module('mediaqPoi')
  .controller('MainCtrl', function ($scope, $resource, uiGmapGoogleMapApi) {

    uiGmapGoogleMapApi.then(function (maps) {
      $scope.map = {
        center: {
        latitude: 48.150529,
        longitude: 11.595077
        },
        zoom: 15
      };
      $scope.options = {
        mapTypeControl: false,
        zoomControl: true,
        zoomControlOptions: {
          style: maps.ZoomControlStyle.LARGE,
          position: maps.ControlPosition.LEFT_CENTER
        },
        panControl: false,
        streetViewControl: false
      };

      $resource('/videos').get(function (data) {
        $scope.videos = [];
        for (var i in data.videos) {
          var video = data.videos[i];
          var path = [];
          for (var j in video.trajectory) {
            var trajectoryPoint = video.trajectory[j];
            path.push({
              latitude: trajectoryPoint.latitude,
              longitude: trajectoryPoint.longitude
            });
          }
          video.path = path;
          video.pathStyle = {
            id: i,
            path: path,
            stroke: {
              color: '#6060FB',
              weight: 3
            },
            editable: true,
            draggable: true,
            geodesic: true,
            visible: true
          };
          $scope.videos.push(video);
        }
      });
    });

  });
