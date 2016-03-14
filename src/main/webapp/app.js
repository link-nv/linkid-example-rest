angular.module('app', ['ngResource','linkIDQR'])

.controller('HomeCtrl', function($scope) {

    $scope.title = 'linkID REST example project'

    $scope.successMessage = function(){

        alert('success!');
    }

});
