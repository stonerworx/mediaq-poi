'use strict';

/**
 * @ngdoc function
 * @name mediaqPoi.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of mediaqPoi
 */
angular.module('mediaqPoi')
  .controller('AboutCtrl', function ($scope, $http, $Showdown) {
    $http.get('views/readme.md')
      .then(function(res){
        $scope.markdown = $Showdown.makeHtml(res.data);
      });
  });
