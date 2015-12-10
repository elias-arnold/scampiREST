var restUrl = "http://localhost:8080/dyn/";

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

app.controller('loaderCtrl', ['$scope', '$http', function($scope, $http) {
	$scope.helloText = "Hello World";
	
	$scope.messages = {};
	$scope.getServices = function (){
		var request = {
	            method:  'GET',
	            url:     restUrl + 'service',
	            headers: {'Content-Type' : 'application/json'}
	        };
        $http(request).success(function (data) {
        	$scope.messages = data;
        }).error(function(data){
            console.log('Error retrieving messages');
            console.log(data);
        });
	}
	$scope.getServices();
	
	
	$scope.getPathForService = function(serviceName){
		return restUrl + "service/" + serviceName + "/random"
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
