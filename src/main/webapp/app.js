angular.module('app', ['ngResource','ng-linkIDQR'])

.controller('HomeCtrl', function($scope) {

    $scope.title = 'linkID REST example project'

    $scope.successMessage = function(){

        alert('success!');
    }

    $scope.successPaymentMessage = function(){

        alert('payment success!');
    }

});
