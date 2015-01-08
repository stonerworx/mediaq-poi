'use strict';

/**
 * @ngdoc function
 * @name mediaqPoi.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of mediaqPoi
 */
angular.module('mediaqPoi')
  .controller('MainCtrl', function ($scope, uiGmapGoogleMapApi) {

    uiGmapGoogleMapApi.then(function(maps) {
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
    });
  });
