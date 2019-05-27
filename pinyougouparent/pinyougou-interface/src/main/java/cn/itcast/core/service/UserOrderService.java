package cn.itcast.core.service;

import entity.PageResult;


public interface UserOrderService {

    PageResult findAllOrders(Integer page, Integer rows, String name);
}
