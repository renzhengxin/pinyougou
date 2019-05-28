//地址控制层
app.controller('addressController', function ($scope, addressService) {


    $scope.showAddress = function () {
        addressService.showAddress().success(
            function (response) {
                $scope.entity = response;
            });
    }

    // 查询一级分类列表:
    $scope.selectProvinceList = function () {
        addressService.findProvinces().success(function (response) {
            $scope.provincesList = response;
        });
    }

    // 查询二级分类列表:
    $scope.$watch("address.provinceId", function (newValue, oldValue) {
        $scope.areasList = [];
        addressService.findCities(newValue).success(function (response) {
            $scope.citiesList = response;
        });
    });

    // 查询三级分类列表:
    $scope.$watch("address.cityId", function (newValue, oldValue) {

        addressService.findAreas(newValue).success(function (response) {
            $scope.areasList = response;
        });
    });

    $scope.save = function () {
        addressService.add($scope.address).success(
            function (response) {
                if (response.flag) {
                    //重新查询
                    alert(response.message);
                    location.reload();
                } else {
                    alert(response.message);
                }
            }
        );
    }

    $scope.del = function (id) {
        addressService.delete(id).success(
            function (response) {
                if (response.flag) {
                    //重新查询
                    alert(response.message);
                    location.reload();
                } else {
                    alert(response.message);
                }
            });
    }
});