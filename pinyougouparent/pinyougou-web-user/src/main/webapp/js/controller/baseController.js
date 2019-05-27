app.controller("baseController",function($scope){
	// 分页的配置的信息
	$scope.paginationConf = {
		 currentPage: 1, // 当前页数
		 totalItems: 0, // 总记录数
		 itemsPerPage: 5, // 每页显示多少条记录
		 perPageOptions: [5,10, 20, 30, 40, 50],// 显示多少条下拉列表
		 onChange: function(){ // 当页码、每页显示多少条下拉列表发生变化的时候，自动触发了
			$scope.reloadList();// 重新加载列表
		 }
	};

	$scope.reloadList = function(){
		// $scope.findByPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
		$scope.findAllOrders($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}

    $scope.status=["","未付款","已付款","未发货","已发货","交易成功","交易关闭","待评价"];
});