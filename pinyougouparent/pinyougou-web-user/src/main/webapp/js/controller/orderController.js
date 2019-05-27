//订单控制层
app.controller('orderController', function ($scope, $controller, orderService) {

    $controller('baseController', {$scope: $scope});//继承

    $scope.findAllOrders = function (page,rows) {
        orderService.findAllOrders(page, rows).success(
            function (response) {
                $scope.paginationConf.totalItems = response.total;
                $scope.list = response.rows;
            });
    }


});