'use strict';

/**
 * @ngdoc function
 * @name frontendApp.services:LoaderService
 * @description
 * # LoaderService
 *
 */

function LoaderService($rootScope)
{

  var loading = {};

  this.startLoading = function(id) {
    var count = loading[id];
    if (count === undefined) {
      count = 0;
    }
    loading[id] = count + 1;
    $rootScope.loading = true;
  };

  this.stopLoading = function(id) {
    var count = loading[id];
    if (count-1 === 0) {
      delete loading[id];
    }

    if (Object.keys(loading).length === 0) {
      $rootScope.loading = false;
    }
  };

  return this;
}

angular.module('mediaqPoi')
  .factory('LoaderService', ['$rootScope', LoaderService]);
