(function() {

  'use strict';

  function config($routeProvider, uiGmapGoogleMapApiProvider, $logProvider, $httpProvider) {
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
      v: '3.17'
    });
    $logProvider.debugEnabled(true);
    $httpProvider.interceptors.push('HttpInterceptor');
  }

  angular
    .module('mediaqPoi', [
              'ngResource',
              'ngRoute',
              'ngSanitize',
              'uiGmapgoogle-maps',
              'Showdown'
            ])
    .config(config);

})();
