(function() {

  'use strict';

  function dataService($resource, $log, $q) {

    var host = '';
    if (window.location.host === 'localhost:9000') {
      host = 'http://localhost:8888';
    }

    function getVideos() {
      var deferred = $q.defer();

      $log.info('requesting videos.');

      $resource(host + '/videos').get(function (data) {

        $log.info('videos received (' + data.videos.length + '). returning.');

        deferred.resolve(data.videos);
      });

      return deferred.promise;
    }

    function getVideo(id) {
      var deferred = $q.defer();

      $log.info('requesting video ' + id + '.');

      $resource(host + '/video/' + id).get(function (data) {

        $log.info('video received. returning.');

        deferred.resolve(data);
      });

      return deferred.promise;
    }

    return {
      getVideos: getVideos,
      getVideo: getVideo
    };

  }

  angular.module('mediaqPoi')
    .factory('dataService', ['$resource', '$log', '$q', dataService]);

})();
