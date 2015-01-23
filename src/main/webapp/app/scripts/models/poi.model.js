(function() {

  'use strict';

  function PoiModelFactory() {

    function PoiModel(poi) {

      var id = poi.id;

      var name = poi.name;
      var latitude = poi.latitude;
      var longitude = poi.longitude;

      this.getId = function() {
        return id;
      };
      this.getName = function() {
        return name;
      };
      this.getLatitude = function() {
        return latitude;
      };
      this.getLongitude = function() {
        return longitude;
      };
      this.getMarker = function() {
        return {
          id: this.getId(),
          icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
          latitude: this.getLatitude(),
          longitude: this.getLongitude(),
          options: { title: this.getName() },
        };
      };
    }

    return PoiModel;

  }

  angular.module('mediaqPoi')
    .factory('PoiModel', [PoiModelFactory]);

})();
