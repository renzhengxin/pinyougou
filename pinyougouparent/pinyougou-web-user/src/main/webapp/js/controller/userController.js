 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	$scope.entity = {};
	//注册用户
	$scope.reg=function(){
		
		//比较两次输入的密码是否一致
		if($scope.password!=$scope.entity.password){
			alert("两次输入密码不一致，请重新输入");
			$scope.entity.password="";
			$scope.password="";
			return ;			
		}
		//新增
		userService.add($scope.entity,$scope.smscode).success(
			function(response){
				alert(response.message);
				// location.href="http://localhost:9104/home-index.html";
			}		
		);
	}
    
	//发送验证码
	$scope.sendCode=function(){
		if($scope.entity.phone==null || $scope.entity.phone==""){
			alert("请填写手机号码");
			return ;
		}
		
		userService.sendCode($scope.entity.phone  ).success(
			function(response){
				alert(response.message);
			}
		);		
	}

	$scope.findUser=function () {
		userService.findUser().success(
			function (response) {
				$scope.user=response;
            }
		)
    }

    $scope.save=function (user) {
        userService.save(user).success(
            function (response) {
                alert(response.message);
            }
        )
    }
	
});	
