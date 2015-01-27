(function() {

  'use strict';

  function dataService($resource, $log, $q, VideoModel, PoiModel) {

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

        if (data.videos !== undefined) {
          $log.info('videos received (' + data.videos.length + '). returning.');
        } else {
          $log.error('failed to load videos.');
        }

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

      var url = host + '/video/' + id;

      $log.debug(url);

      $resource(url).get(function (data) {

        $log.info('video received. returning.');

        var video = new VideoModel(data.video);

        video.setCenter(data.center);
        video.setRadius(data.searchRange);

        var nearbyPois = [];
        angular.forEach(data.nearbyPois, function(poi) {
          nearbyPois.push(new PoiModel(poi));
        });
        video.setNearbyPois(nearbyPois);

        var visiblePois = [];
        angular.forEach(data.visiblePois, function(poi) {
          visiblePois.push(new PoiModel(poi));
        });
        video.setVisiblePois(visiblePois);

        var timeline = [];
        angular.forEach(data.timeline, function(pois, second) {
          var poiObjects = [];
          angular.forEach(pois, function(poi) {
            poiObjects.push(new PoiModel(poi));
          });
          timeline[second] = poiObjects;
        });
        video.setTimeline(timeline);

        video.setPosTimeline(data.posTimeline);

        deferred.resolve(video);
      });

      return deferred.promise;
    }

    function getPoi(id) {
      var deferred = $q.defer();

      $log.info('requesting poi ' + id + '.');

      var url = host + '/poi/' + id;

      $log.debug(url);

      $resource(url).get(function (data) {

        $log.info('poi received. returning.');

        var poi = new PoiModel(data.poi);
        var videoList = [];
        angular.forEach(data.videos, function(video) {
          videoList.push(new VideoModel(video));
        });
        poi.setVideos(videoList);

        deferred.resolve(poi);
      });

      return deferred.promise;
    }

    return {
      getVideos: getVideos,
      getVideo: getVideo,
      getPoi: getPoi
    };

  }

  angular.module('mediaqPoi')
    .factory('dataService', ['$resource', '$log', '$q', 'VideoModel', 'PoiModel', dataService]);

})();
