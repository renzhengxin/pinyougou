//服务层
app.service('addressService',function($http){
    //读取列表数据绑定到表单中
    this.showAddress=function(){
        return $http.get('../address/findListByLoginUser.do');
    }

    this.findProvinces = function(){
        return $http.get("../address/findProvinces.do");
    }
    this.findCities = function(provinceId){
        return $http.get("../address/findCities.do?provinceId="+provinceId);
    }
    this.findAreas = function(cityId){
        return $http.get("../address/findAreas.do?cityId="+cityId);
    }

    this.add=function(address){
        return  $http.post('../address/add.do',address );
    }

    this.delete=function(id){
        return  $http.get('../address/delete.do?id='+id);
    }
});