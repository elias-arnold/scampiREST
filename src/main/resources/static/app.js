var restUrl = "http://myliberouter.org/dyn/";

var app = angular.module('scampiREST', [
    'ngRoute'
]);

app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider.otherwise({redirectTo: '/loader'});
    $routeProvider.when('/loader', {
        templateUrl: 'loader.html',
        controller: 'loaderCtrl'
      });
    $routeProvider.when('/loader/:serviceName', {
        templateUrl: 'loader.html',
        controller: 'serviceLoaderCtrl'
      });
}]);

app.controller('appController', ['$scope', function($scope) {
	$scope.welcomeText = "Scampi Restinterface Loader";
}]);

app.controller('loaderCtrl', ['$scope', '$http','$window', function($scope, $http, $window) {
	$scope.helloText = "Hello World";
	
	$scope.services = {};
	$scope.getServices = function (){
		var request = {
	            method:  'GET',
	            url:     restUrl + 'service',
	            headers: {'Content-Type' : 'application/json'}
	        };
        $http(request).success(function (data) {
        	$scope.services = data;
        }).error(function(data){
            console.log('Error retrieving messages');
            console.log(data);
        });
	}
	$scope.getServices();
	
	
	$scope.getPathForService = function(message){
		var serviceName = message.name;
		if (message.currentId != undefined) {
			$window.location.href = restUrl + "service/" + serviceName + "/" + message.currentId;
		} else {
			$window.location.href = restUrl + "service/" + serviceName + "/random";
		}
	}
	
}]);

app.controller('serviceLoaderCtrl', ['$scope', '$routeParams', '$http', function($scope, $routeParams, $http) {
	$scope.serviceName = $routeParams.serviceName;
	$scope.helloText = "Hello World";
	
	$scope.messages = {};
	$scope.getMessages = function (){
		var request = {
	            method:  'GET',
	            url:     restUrl + 'service/' + $scope.serviceName,
	            headers: {'Content-Type' : 'application/json'}
	        };
        $http(request).success(function (data) {
        	$scope.messages = data;
        }).error(function(data){
            console.log('Error retrieving messages');
            console.log(data);
        });
	}
	$scope.getMessages();
	
}]);
