'use strict';

/**
 * @ngdoc overview
 * @name mediaqPoi
 * @description
 * # mediaqPoi
 *
 * Main module of the application.
 */
angular
  .module('mediaqPoi', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'uiGmapgoogle-maps',
    'Showdown'
  ])
  .config(function ($routeProvider, uiGmapGoogleMapApiProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
    uiGmapGoogleMapApiProvider.configure({
      key: 'AIzaSyDIZgzM1EkYHEhOJfcjUvm0ovOUczk7v8s',
      v: '3.17',
      libraries: 'weather,geometry,visualization'
    });
  });
