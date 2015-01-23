(function() {

  'use strict';

  function dataService($resource, $log, $q, VideoModel) {

    var host = '';
    if (window.location.host === 'localhost:9000') {
      host = 'http://localhost:8888';
    }

    function getVideos(bounds) {
      var deferred = $q.defer();

      $log.info('requesting videos with bounds: NW {' + bounds.northEast.latitude + ', ' +
        bounds.northEast.longitude + '}. SE {' + bounds.southWest.latitude + ', ' +
        bounds.southWest.longitude + '}');

      var url = host + '/videos?action=range_query&bound1_lat=' + bounds.northEast.latitude +
                '&bound1_lng=' + bounds.northEast.longitude + '&bound2_lat=' +
                bounds.southWest.latitude + '&bound2_lng=' + bounds.southWest.longitude;

      $log.debug(url);

      $resource(url).get(function (data) {

        $log.info('videos received (' + data.videos.length + '). returning.');

        var videos = [];

        angular.forEach(data.videos, function(video) {
          videos.push(new VideoModel(video));
        });

        deferred.resolve(videos);
      });

      return deferred.promise;
    }

    function getVideo(id) {
      var deferred = $q.defer();

      $log.info('requesting video ' + id + '.');

      $resource(host + '/video/' + id).get(function (data) {

        $log.info('video received. returning.');

        var video = new VideoModel(data.video);
        video.setDetails(data);

        deferred.resolve(video);
      });

      return deferred.promise;
    }

    return {
      getVideos: getVideos,
      getVideo: getVideo
    };

  }

  angular.module('mediaqPoi')
    .factory('dataService', ['$resource', '$log', '$q', 'VideoModel', dataService]);

})();
