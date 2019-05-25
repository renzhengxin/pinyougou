app.controller("indexController",function($scope,loginService,orderService){
    $scope.ordersList = [];
	$scope.showName = function(){
		loginService.showName().success(function(response){
			$scope.loginName = response.username;
			$scope.loginTime=response.cur_time;
		});
	}

	//订单查询
    $scope.findAllOrders = function(){
        loginService.findAllOrders().success(function(response){
            $scope.ordersList = response;
        });
    }
});