package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;

public interface CollectService {
    Item findItemById(Long itemId);

    void collectGoods(String name, Item item);
//    void collectGoods(Long itemId);
}
