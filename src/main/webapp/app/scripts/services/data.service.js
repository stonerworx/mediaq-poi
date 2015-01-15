(function() {

  'use strict';

  function dataService($resource, $log, $q) {
    return {
      getVideos: getVideos
    };

    function getVideos() {
      var deferred = $q.defer();

      $log.info('requesting videos.');

      $resource('/videos').get(function (data) {

        $log.info('videos received (' + data.videos.length + '). returning.');

        deferred.resolve(data.videos);
      });

      return deferred.promise;
    }

  }

  angular.module('mediaqPoi')
    .factory('dataService', ['$resource', '$log', '$q', dataService]);

})();
