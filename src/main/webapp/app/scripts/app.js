(function() {

  'use strict';

  function config($routeProvider, uiGmapGoogleMapApiProvider, $logProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainController',
        controllerAs: 'vm'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutController',
        controllerAs: 'vm'
      })
      .otherwise({
        redirectTo: '/'
      });
    uiGmapGoogleMapApiProvider.configure({
      key: 'AIzaSyDIZgzM1EkYHEhOJfcjUvm0ovOUczk7v8s',
      v: '3.17',
      libraries: 'weather,geometry,visualization'
    });
    $logProvider.debugEnabled(true);
  }

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
    .config(config);

})();
