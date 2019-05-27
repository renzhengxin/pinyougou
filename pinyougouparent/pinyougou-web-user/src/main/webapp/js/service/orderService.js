app.service("orderService", function ($http) {

    this.findAllOrders = function (page, rows) {
        return $http.post("../order/findAllOrders.do?page=" + page + "&rows=" + rows);
    }

});