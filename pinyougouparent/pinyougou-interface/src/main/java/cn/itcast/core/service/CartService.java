package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import vo.Cart;

import java.util.List;

public interface CartService {
    Item findItemById(Long itemId);

    void addCartListToRedis(List<Cart> cartList, String name);

    List<Cart> findCartListFromRedis(String name);

    List<Cart> findAllCartList(List<Cart> cartList);
}
