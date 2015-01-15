(function() {

  'use strict';

  function AboutController($http, Showdown) {
    var vm = this;

    $http.get('views/readme.md')
      .then(function (res) {
        vm.markdown = Showdown.makeHtml(res.data);
      });
  }

  angular.module('mediaqPoi')
    .controller('AboutController', ['$http', '$Showdown', AboutController]);

})();
