package cn.itcast.core.controller;

import cn.itcast.core.service.UserOrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private UserOrderService userOrderService;

    @RequestMapping("/findAllOrders")
    public PageResult findAllOrders(Integer page, Integer rows){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return userOrderService.findAllOrders(page,rows,name);
    }
}
