package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@Service
public class CollectServiceImpl implements CollectService {
    @Autowired
    ItemDao itemDao;
    @Autowired
    RedisTemplate redisTemplate;

    //gen'j
    @Override
    public Item findItemById(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }


    //根据用户名，进行收藏商品缓存
    @Override
    public void collectGoods(String name, Item item) {
        redisTemplate.boundValueOps(name).set(item);
    }


//    //用户添加收藏
//    @Override
//    public void collectGoods(Item item) {
//        //根据id查询库存对象
//
//        //根据用户名进行缓存
//
//    }
}
