'use strict';

/**
 * @ngdoc function
 * @name frontendApp.services:HttpInterceptor
 * @description
 * # HttpInterceptor
 *
 */

function HttpInterceptor($q, $log, LoaderService)
{
  var interceptor = {
    request: function (config) {
      LoaderService.startLoading(config.url);
      return config;
    },
    response: function (response) {
      LoaderService.stopLoading(response.config.url);
      return response;
    },
    requestError: function (rejection) {
      LoaderService.stopLoading(rejection.config.url);
      $log.error('Response Error: ' + rejection.status + ' - ' + rejection.statusText);
      return $q.reject(rejection);
    },
    responseError: function (rejection) {
      LoaderService.stopLoading(rejection.config.url);
      $log.error('Response Error: ' + rejection.status + ' - ' + rejection.statusText);
      return $q.reject(rejection);
    }
  };
  return interceptor;
}

angular.module('mediaqPoi')
  .factory('HttpInterceptor', ['$q', '$log', 'LoaderService', HttpInterceptor]);
