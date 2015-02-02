'use strict';

describe('MainController', function () {

  // load the controller's module
  beforeEach(module('mediaqPoi'));

  var MainController,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    MainController = $controller('MainController', {
      $scope: scope
    });
  }));

  it('should start loading', function () {
    expect(MainController.loading).toBeTruthy();
  });
});
